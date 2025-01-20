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
 * WebSocket handler that manages chat flow interactions (Approach B).
 * The start node is skipped, so we set currentNodeId to the next node
 * and only process that node on the subsequent message.
 */
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Tracks active sessions and their contexts
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
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

    @Override
    @SuppressWarnings("unchecked")
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            // Convert incoming JSON to a Map
            String payload = message.getPayload();
            Map<String, Object> incomingData = objectMapper.readValue(payload, Map.class);

            // Extract the flowId and userResponse
            String flowId = (String) incomingData.getOrDefault("flowId", "");
            String userResponse = (String) incomingData.getOrDefault("userResponse", "");
            System.out.println("Received message: " + userResponse + " for flowId: " + flowId);

            // Session-specific context
            Map<String, Object> context = sessionContexts.computeIfAbsent(
                    session.getId(),
                    k -> new HashMap<>()
            );

            // If no flowId is provided, just echo back
            if (flowId == null || flowId.isEmpty()) {
                sendBotMessage(session, "No flowId provided. Echo: " + userResponse);
                return;
            }

            // Load the Flow
            Flow flow;
            try {
                flow = flowService.getFlow(flowId);
            } catch (Exception e) {
                sendBotMessage(session, "Error: Flow not found for ID: " + flowId);
                return;
            }

            // Store user input in context
            context.put("userResponse", userResponse);

            // Check if we already have a current node
            String currentNodeId = (String) context.get("currentNodeId");

            // ---------------------------------------------------------------------
            // If currentNodeId == null, this is the first user message for this flow
            // We skip the start node AND immediately process the next node
            // so the user sees its response right away.
            // ---------------------------------------------------------------------
            if (currentNodeId == null) {
                // 1) Find the start node
                Node startNode = flow.getNodes().stream()
                        .filter(n -> "startNode".equals(n.getType()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Start node not found in flow"));

                System.out.println("Skipping start node: " + startNode.getId());

                // 2) Find the node after the start node
                String nextNodeId = findFirstTargetNodeId(flow, startNode.getId());
                if (nextNodeId == null) {
                    sendBotMessage(session, "No next node found after start node. Flow halted.");
                    return;
                }

                Node nextNode = flow.getNodes().stream()
                        .filter(n -> n.getId().equals(nextNodeId))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Next node not found: " + nextNodeId));

                // 3) Immediately process that next node, so its output is shown right away
                System.out.println("Auto-processing node after start: " + nextNodeId);
                String followingNodeId = flowExecutor.processNode(flow, nextNode, context, session);

                // 4) If that node leads to yet another node, store it in the context
                if (followingNodeId != null) {
                    context.put("currentNodeId", followingNodeId);
                    System.out.println("Flow paused at node: " + followingNodeId);
                } else {
                    // Flow ended right after the first real node
                    sendBotMessage(session, "Flow completed right after the first node.");
                }

                return;
            }

            // ---------------------------------------------------------------------
            // Otherwise, we process the current node as usual
            // ---------------------------------------------------------------------
            Node currentNode = flow.getNodes().stream()
                    .filter(n -> n.getId().equals(currentNodeId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Node not found: " + currentNodeId));

            System.out.println("Processing current node: " + currentNodeId);

            String nextNodeId = flowExecutor.processNode(flow, currentNode, context, session);
            if (nextNodeId != null) {
                context.put("currentNodeId", nextNodeId);
                System.out.println("Moved to next node: " + nextNodeId);
            } else {
                // Flow ended
                sendBotMessage(session, "Flow completed.");
                context.remove("currentNodeId");
            }

        } catch (Exception e) {
            e.printStackTrace();
            try {
                sendBotMessage(session, "Error handling message: " + e.getMessage());
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }


    /**
     * Helper: find the first edge whose source == startNodeId, return target node ID
     */
    private String findFirstTargetNodeId(Flow flow, String startNodeId) {
        if (flow.getEdges() == null || flow.getEdges().isEmpty()) {
            return null;
        }
        return flow.getEdges().stream()
                .filter(e -> e.getSource().equals(startNodeId))
                .map(Flow.Edge::getTarget)
                .findFirst()
                .orElse(null);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
        sessionContexts.remove(session.getId());
        System.out.println("WebSocket connection closed: " + session.getId());
    }

    /**
     * Simple utility to send a message to the client (sender=bot).
     */
    private void sendBotMessage(WebSocketSession session, String text) throws IOException {
        Map<String, String> response = Map.of(
                "sender", "bot",
                "message", text
        );
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }
}
