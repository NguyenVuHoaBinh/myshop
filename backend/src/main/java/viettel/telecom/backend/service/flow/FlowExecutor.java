package viettel.telecom.backend.service.flow;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import viettel.telecom.backend.entity.flow.Flow;
import viettel.telecom.backend.entity.flow.Flow.Node;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A single-step flow executor that uses global edges (Approach B).
 * We skip the startNode externally and call processNode(...) for each user turn.
 */
@Service
public class FlowExecutor {

    private static final Logger logger = LoggerFactory.getLogger(FlowExecutor.class);

    private final InteractionHandler interactionHandler;
    private final DataHandler dataHandler;
    private final LLMHandler llmHandler;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public FlowExecutor(
            InteractionHandler interactionHandler,
            DataHandler dataHandler,
            LLMHandler llmHandler
    ) {
        this.interactionHandler = interactionHandler;
        this.dataHandler = dataHandler;
        this.llmHandler = llmHandler;
    }

    /**
     * Process a single node, performing its action and returning the ID
     * of the next node (or null if the flow ends here).
     *
     * @param flow    The entire Flow, so we can look up edges
     * @param node    The current Node to process
     * @param context Execution context (including user response, etc.)
     * @param session WebSocket session (may be null if not using websockets)
     * @return Next node ID, or null if no next node or flow ended
     */
    public String processNode(Flow flow, Node node, Map<String, Object> context, WebSocketSession session) {
        try {
            logger.debug("processNode() - ID: {}, Type: {}", node.getId(), node.getType());

            // 1) Perform node-specific logic
            switch (node.getType()) {
                case "interactionNode":
                    // Send a bot response (if we have a WebSocket session)
                    String botResponse = node.getData().getBotResponse();
                    if (botResponse != null && !botResponse.isEmpty()) {
                        sendMessage(session, botResponse);
                        logger.info("Sent bot response: {}", botResponse);
                    }

                    // Capture user response from context (if provided)
                    String userResponse = (String) context.get("userResponse");
                    if (userResponse != null) {
                        logger.info("User response received: {}", userResponse);
                        // store or handle as needed
                        context.put("userResponse", userResponse);
                    }
                    break;

                case "dataNode":
                    // Example: read/write data from context or external source
                    dataHandler.handle(node, context);
                    logger.info("Data node processed: {}", node.getId());
                    break;

                case "llmNode":
                    // Example: call an LLM with some prompt
                    llmHandler.handle(node, context, session);
                    String llmResponse = (String) context.get("llmResponse");
                    if (llmResponse != null && session != null && session.isOpen()) {
                        sendMessage(session, llmResponse);
                        logger.info("LLM response sent: {}", llmResponse);
                    }
                    break;

                case "endNode":
                    // End node => no next node
                    logger.info("Reached end node: {}", node.getId());
                    sendMessage(session, "Flow completed at end node: " + node.getId());
                    return null;

                default:
                    logger.warn("Unknown node type encountered: {}", node.getType());
                    sendMessage(session, "Unknown node type: " + node.getType());
                    throw new IllegalArgumentException("Unknown node type: " + node.getType());
            }

            // 2) Find the next node via global edges
            Node nextNode = getNextNode(flow, node);
            if (nextNode == null) {
                // If there's no next node, we consider the flow ended
                logger.info("No more edges from node: {} => flow ended.", node.getId());
                sendMessage(session, "Flow ended. No further nodes.");
                return null;
            }
            return nextNode.getId();

        } catch (Exception e) {
            logger.error("Error processing node: {}. Details: {}", node.getId(), e.getMessage(), e);
            sendMessage(session, "Error processing node: " + node.getId());
            throw new RuntimeException("Error processing node: " + e.getMessage(), e);
        }
    }

    /**
     * Locates the next node by searching for the first edge whose
     * source == currentNode's ID, then retrieving that edge's target node.
     */
    private Node getNextNode(Flow flow, Node currentNode) {
        return flow.getEdges().stream()
                .filter(edge -> edge.getSource().equals(currentNode.getId()))
                .map(edge -> getNodeById(flow, edge.getTarget()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Convenience method to find a node by ID within the flow's node list.
     */
    private Node getNodeById(Flow flow, String nodeId) {
        return flow.getNodes().stream()
                .filter(n -> n.getId().equals(nodeId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Send a message via WebSocket if session is available. Safe to call with null.
     */
    private void sendMessage(WebSocketSession session, String message) {
        if (session == null || !session.isOpen()) {
            logger.debug("No open session available; cannot send message: {}", message);
            return;
        }
        try {
            Map<String, String> response = new HashMap<>();
            response.put("sender", "bot");
            response.put("message", message);

            String jsonResponse = objectMapper.writeValueAsString(response);
            session.sendMessage(new TextMessage(jsonResponse));
        } catch (IOException e) {
            logger.error("Failed to send WebSocket message. Details: {}", e.getMessage(), e);
        }
    }
}
