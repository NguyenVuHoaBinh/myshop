package viettel.telecom.backend.service.redis.memory;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.AbstractTransaction;
import redis.clients.jedis.exceptions.JedisException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service that manages chat memory in Redis, including session metadata & chat history.
 */
@Service
public class ChatMemoryService {

    private static final Logger logger = LoggerFactory.getLogger(ChatMemoryService.class);

    private static final String SESSION_METADATA_PREFIX = "session:metadata:";
    private static final String CHAT_HISTORY_PREFIX = "session:chat:";

    private static final int METADATA_EXPIRATION = 24 * 3600; // 24 hours
    private static final int CHAT_HISTORY_EXPIRATION = 7 * 24 * 3600; // 7 days

    private final JedisPooled jedisPooled;
    private final ObjectMapper objectMapper;

    @Autowired
    public ChatMemoryService(JedisPooled jedisPooled) {
        this.jedisPooled = jedisPooled;
        this.objectMapper = new ObjectMapper();
    }

    public void storeSessionMetadata(String sessionId, Map<String, String> metadata) {
        if (!isValidSessionId(sessionId) || metadata == null) {
            logger.warn("Invalid sessionId or metadata. sessionId={}", maskSessionId(sessionId));
            return;
        }
        String redisKey = SESSION_METADATA_PREFIX + sessionId;
        try {
            AbstractTransaction transaction = jedisPooled.multi();
            transaction.hset(redisKey, metadata);
            transaction.expire(redisKey, METADATA_EXPIRATION);
            transaction.exec();
        } catch (JedisException e) {
            logger.error("Failed to store session metadata. sessionId={}", maskSessionId(sessionId), e);
        }
    }

    /**
     * Stores a chat message as JSON, removing any triple backtick code fences first.
     */
    public void storeUserChat(String sessionId, String userType, String message) {
        if (!isValidSessionId(sessionId) || !isValidUserType(userType) || !isValidMessage(message)) {
            logger.warn("Invalid input for storing user chat. sessionId={}, userType={}",
                    maskSessionId(sessionId), userType);
            return;
        }

        // Strip code fences (```...```) if present
        String cleaned = removeCodeFences(message);

        // Build a JSON record: { "role", "timestamp", "content" }
        Map<String, Object> record = new HashMap<>();
        record.put("role", userType);
        record.put("timestamp", System.currentTimeMillis());
        record.put("content", cleaned);

        String redisKey = CHAT_HISTORY_PREFIX + sessionId;
        try {
            String jsonMsg = objectMapper.writeValueAsString(record);
            AbstractTransaction transaction = jedisPooled.multi();
            transaction.rpush(redisKey, jsonMsg);
            transaction.expire(redisKey, CHAT_HISTORY_EXPIRATION);
            transaction.exec();
        } catch (Exception e) {
            logger.error("Failed to store chat message. sessionId={}, userType={}, error={}",
                    maskSessionId(sessionId), userType, e.getMessage(), e);
        }
    }

    /**
     * Get all stored chat messages for this session (each is a JSON string).
     */
    public List<String> getUserChat(String sessionId) {
        if (!isValidSessionId(sessionId)) {
            return new ArrayList<>();
        }
        String redisKey = CHAT_HISTORY_PREFIX + sessionId;
        try {
            return jedisPooled.lrange(redisKey, 0, -1);
        } catch (JedisException e) {
            logger.error("Failed to retrieve chat. sessionId={}, error={}", maskSessionId(sessionId), e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get the very last (most recent) chat message in this session. Returns null if none.
     */
    public String getLastChatMessage(String sessionId) {
        if (!isValidSessionId(sessionId)) {
            return null;
        }
        String redisKey = CHAT_HISTORY_PREFIX + sessionId;
        try {
            return jedisPooled.lindex(redisKey, -1);
        } catch (JedisException e) {
            logger.error("Failed to retrieve last chat message. sessionId={}, error={}",
                    maskSessionId(sessionId), e.getMessage());
            return null;
        }
    }

    // Example "remove fences" code
    private String removeCodeFences(String input) {
        if (input == null) return "";
        String trimmed = input.trim();
        // If it starts with ``` and ends with ```
        if (trimmed.startsWith("```") && trimmed.endsWith("```")) {
            trimmed = trimmed.substring(3).trim(); // remove leading ```
            if (trimmed.toLowerCase().startsWith("json")) {
                trimmed = trimmed.substring(4).trim(); // remove 'json'
            }
            if (trimmed.endsWith("```")) {
                trimmed = trimmed.substring(0, trimmed.length() - 3).trim(); // remove trailing ```
            }
        }
        return trimmed;
    }

    // Session ID, user type, message validations
    private boolean isValidSessionId(String sid) {
        return sid != null && !sid.trim().isEmpty();
    }
    private boolean isValidUserType(String userType) {
        return "user".equals(userType) || "assistant".equals(userType);
    }
    private boolean isValidMessage(String msg) {
        return msg != null && !msg.trim().isEmpty();
    }
    private String maskSessionId(String sid) {
        if (sid == null) return "null";
        if (sid.length() <= 4) return "****";
        return "****" + sid.substring(sid.length() - 4);
    }
}
