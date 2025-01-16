package viettel.telecom.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import viettel.telecom.backend.entity.flow.Flow;
import viettel.telecom.backend.entity.flow.Flow.Node;
import viettel.telecom.backend.service.flow.FlowExecutor;
import viettel.telecom.backend.service.flow.FlowService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A WebSocket handler that processes chat messages for a flow-based chatbot.
 */
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Stores WebSocket sessions by sessionId (if needed).
     * Key: sessionId, Value: WebSocketSession
     */
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    /**
     * Stores a context map for each session, so we know
     * which node is "current" for that user's conversation.
     * Key: sessionId, Value: a Map with flow data, e.g. { "currentNodeId": "123", ... }
     */
    private final Map<String, Map<String, Object>> sessionContexts = new ConcurrentHashMap<>();

    private final FlowService flowService;
    private final FlowExecutor flowExecutor;

    public ChatWebSocketHandler(FlowService flowService, FlowExecutor flowExecutor) {
        this.flowService = flowService;
        this.flowExecutor = flowExecutor;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);
        System.out.println("New WebSocket connection: " + session.getId());
    }

    /**
     * Called whenever a text message arrives from the client.
     * The payload typically contains JSON with "flowId" and "userResponse".
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            // Parse incoming JSON
            String payload = message.getPayload();
            @SuppressWarnings("unchecked")
            Map<String, Object> incomingData = objectMapper.readValue(payload, Map.class);

            // Extract flowId, userResponse from the client message
            String flowId = (String) incomingData.getOrDefault("flowId", "");
            String userResponse = (String) incomingData.getOrDefault("userResponse", "");

            // Retrieve or initialize the session context
            Map<String, Object> context = sessionContexts.computeIfAbsent(
                    session.getId(),
                    k -> new HashMap<>()
            );

            if (flowId == null || flowId.isEmpty()) {
                // If no flowId is provided, just echo
                sendBotMessage(session, "No flowId provided. Echo: " + userResponse);
                return;
            }

            // 1. Fetch the flow from Elasticsearch (via FlowService)
            Flow flow;
            try {
                flow = flowService.getFlow(flowId);
            } catch (Exception e) {
                sendBotMessage(session, "Error: Flow not found for ID: " + flowId);
                return;
            }

            // 2. Put user response into context so FlowExecutor can handle it
            context.put("userResponse", userResponse);

            // 3. Determine the current node for this session
            String currentNodeId = (String) context.getOrDefault("currentNodeId", null);

            if (currentNodeId == null) {
                // First time: "start" the flow
                // Option A: Let flowExecutor.executeFlow() run the entire flow
                // Option B: Initialize the current node to the flow's startNode, then do partial execution
                // We'll do partial execution to handle "step-by-step" logic:

                // Find the start node
                Node startNode = flow.getNodes().stream()
                        .filter(n -> "startNode".equals(n.getType()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Start node not found in flow"));

                // We'll store it
                currentNodeId = startNode.getId();
                context.put("currentNodeId", currentNodeId);
            }

            // 4. Process the current node
            final String targetNodeId = currentNodeId; // Explicitly declare as final for clarity
            Node currentNode = flow.getNodes().stream()
                    .filter(n -> n.getId().equals(targetNodeId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Node not found: " + targetNodeId));


            // This calls your existing logic in FlowExecutor
            // which can handle interactionNode, dataNode, llmNode, etc.
            String nextNodeId = flowExecutor.processNode(flow, currentNode, context, session);

            // 5. If there's a "nextNodeId", store it in the context;
            //    otherwise, the flow is done
            if (nextNodeId != null) {
                context.put("currentNodeId", nextNodeId);
            } else {
                // Flow ended, remove currentNodeId from context (or do whatever cleanup you need)
                context.remove("currentNodeId");
            }

        } catch (Exception e) {
            // On any error, notify the user/bot
            try {
                String errorMsg = "Error handling message: " + e.getMessage();
                sendBotMessage(session, errorMsg);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    /**
     * Called when the user closes the WebSocket or it is otherwise disconnected.
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
        sessionContexts.remove(session.getId());
        System.out.println("WebSocket connection closed: " + session.getId());
    }

    /**
     * Utility method to send a "bot" message (simple JSON) to the client.
     */
    private void sendBotMessage(WebSocketSession session, String text) throws IOException {
        Map<String, String> response = Map.of(
                "sender", "bot",
                "message", text
        );
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

}
