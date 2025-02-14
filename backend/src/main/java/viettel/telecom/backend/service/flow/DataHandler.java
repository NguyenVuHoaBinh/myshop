package viettel.telecom.backend.service.flow;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.WebSocketSession;
import viettel.telecom.backend.entity.flow.Flow;
import viettel.telecom.backend.service.redis.memory.ChatMemoryService;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class DataHandler {

    private final RestTemplate restTemplate;
    private final ChatMemoryService chatMemoryService;
    private final MyTokenService myTokenService;   // <-- injected
    private final ObjectMapper mapper = new ObjectMapper();

    public DataHandler(RestTemplate restTemplate,
                       ChatMemoryService chatMemoryService,
                       MyTokenService myTokenService) {
        this.restTemplate = restTemplate;
        this.chatMemoryService = chatMemoryService;
        this.myTokenService = myTokenService;
    }

    public String handle(Flow.Node node, Map<String, Object> context, WebSocketSession session) {
        String successNode = node.getData() != null ? node.getData().getOnSuccessNextNode() : null;
        String errorNode = node.getData() != null ? node.getData().getOnErrorNextNode() : null;

        // 1) Get the sessionId from the WebSocket
        if (session == null) {
            log.warn("No WebSocketSession provided; cannot proceed.");
            return fallbackNode(errorNode);
        }
        String sessionId = session.getId();

        // 2) Load the last chat message from Redis
        String lastJson = chatMemoryService.getLastChatMessage(sessionId);
        if (lastJson == null) {
            log.warn("No last chat message found for sessionId={}", sessionId);
            return fallbackNode(errorNode);
        }

        // 3) Parse top-level record => {role, timestamp, content}
        Map<String, Object> messageMap;
        try {
            messageMap = mapper.readValue(lastJson, Map.class);
        } catch (Exception e) {
            log.warn("Failed to parse last message JSON. sessionId={}, error={}", sessionId, e.getMessage());
            return fallbackNode(errorNode);
        }

        // 4) content might be a string or a map
        Object contentObj = messageMap.get("content");
        Map<String, Object> contentMap = parseAsMap(contentObj);

        // If we fail to parse the content as JSON, we can still do a fallback
        if (contentMap == null) {
            log.info("Content is not JSON; skipping. sessionId={}", sessionId);
            return fallbackNode(errorNode);
        }

        // 5) Extract requestUrl / requestBody from contentMap
        // (If your flow doesn't need these, skip or adapt.)
        String requestUrl = (String) contentMap.get("requestUrl");
        Object bodyObj = contentMap.get("requestBody");

        if (requestUrl == null) {
            log.error("No requestUrl found in contentMap. sessionId={}", sessionId);
            return fallbackNode(errorNode);
        }

        // Attempt to parse requestBody as a map
        Map<String, Object> rawBody = parseAsMap(bodyObj);
        if (rawBody == null) {
            // if it's a string that cannot parse as JSON, or missing
            rawBody = new HashMap<>();
        }

        // Merge everything from contentMap into context for placeholder resolution
        for (Map.Entry<String, Object> entry : contentMap.entrySet()) {
            context.put(entry.getKey(), entry.getValue());
        }

        // 6) Resolve placeholders in the URL and body
        String resolvedUrl = resolvePlaceholdersInString(requestUrl, context);
        Map<String, Object> resolvedBody = resolvePlaceholdersInMap(rawBody, context);

        // 7) Get the Bearer token from MyTokenService (the LLM does NOT provide it)
        String bearerToken = myTokenService.getBearerToken();
        if (bearerToken == null || bearerToken.isEmpty()) {
            log.warn("No bearerToken in application.properties! Proceeding without auth.");
        }

        // 8) Perform the POST request
        try {
            Map<String, Object> responseData;

            if (bearerToken != null && !bearerToken.isEmpty()) {
                // Set the Authorization: Bearer ...
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(bearerToken);

                // Create an HttpEntity with the body + headers
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(resolvedBody, headers);

                ResponseEntity<Map> response = restTemplate.exchange(
                        resolvedUrl,
                        HttpMethod.POST,
                        entity,
                        Map.class
                );
                responseData = response.getBody();
                log.info("POST with Bearer token success => status={}, body={}",
                        response.getStatusCode(), responseData);
            } else {
                // No token => just do a normal postForObject
                responseData = restTemplate.postForObject(resolvedUrl, resolvedBody, Map.class);
                log.info("POST without Bearer token => body={}", responseData);
            }

            // 9) Optionally store the response in context
            if (responseData != null) {
                context.put("lastResponse", responseData);
                log.info("RESPONSE => {}", responseData);
            }

            // Decide next node
            if (successNode != null && !successNode.isEmpty()) {
                return successNode;
            }
            return (node.getNext() != null) ? node.getNext() : "END";

        } catch (Exception ex) {
            log.error("Error while POSTing to {}. sessionId={}, error={}",
                    resolvedUrl, sessionId, ex.getMessage(), ex);
            return fallbackNode(errorNode);
        }
    }

    private String fallbackNode(String errorNode) {
        return (errorNode != null && !errorNode.isEmpty()) ? errorNode : "END";
    }

    /**
     * Attempt to parse an object as a Map. If the object is a Map, cast it.
     * If it's a String, parse as JSON. Returns null if parse fails.
     */
    private Map<String, Object> parseAsMap(Object obj) {
        if (obj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> m = (Map<String, Object>) obj;
            return m;
        } else if (obj instanceof String) {
            String s = (String) obj;
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> m = mapper.readValue(s, Map.class);
                return m;
            } catch (Exception e) {
                log.debug("parseAsMap: Cannot parse string as JSON => {}", e.getMessage());
                return null;
            }
        } else {
            return null;
        }
    }

    private String resolvePlaceholdersInString(String raw, Map<String, Object> context) {
        if (raw == null) return "";
        String patternStr = "\\[([^\\]]+)]";
        Matcher matcher = Pattern.compile(patternStr).matcher(raw);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String placeholder = matcher.group(1);
            Object replacement = context.getOrDefault(placeholder, "");
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement.toString()));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private Map<String, Object> resolvePlaceholdersInMap(Map<String, Object> rawMap, Map<String, Object> context) {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, Object> e : rawMap.entrySet()) {
            Object val = e.getValue();
            if (val instanceof String) {
                result.put(e.getKey(), resolvePlaceholdersInString((String) val, context));
            } else if (val instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nested = (Map<String, Object>) val;
                result.put(e.getKey(), resolvePlaceholdersInMap(nested, context));
            } else {
                // arrays, booleans, numbers remain as-is
                result.put(e.getKey(), val);
            }
        }
        return result;
    }
}
