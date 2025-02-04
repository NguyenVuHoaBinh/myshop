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
                        context.put("userResponse", userResponse);
                    }
                    break;

                case "dataNode":
                    dataHandler.handle(node, context);
                    logger.info("Data node processed: {}", node.getId());
                    break;

                case "llmNode":
                    // Call the LLM handler to generate a response
                    llmHandler.handle(node, context, session);
                    String llmResponse = (String) context.get("llmResponse");

                    // Determine whether to show conversation based on the flag.
                    // Default to true if the flag is not set.
                    Boolean showConversation = (node.getData() != null && node.getData().getShowConversation() != null)
                            ? node.getData().getShowConversation() : Boolean.TRUE;

                    if (Boolean.FALSE.equals(showConversation)) {
                        // If showConversation is false:
                        // 1. Update the context so that the llmResponse becomes the "userResponse"
                        context.put("userResponse", llmResponse);
                        logger.info("LLM node (ID: {}) with showConversation=false. " +
                                "Auto-processing next node with userResponse: {}", node.getId(), llmResponse);

                        // 2. Get the next node and immediately process it.
                        Node nextNode = getNextNode(flow, node);
                        if (nextNode == null) {
                            logger.info("No more edges from node: {} => flow ended.", node.getId());
                            sendMessage(session, "Flow ended. No further nodes.");
                            return null;
                        }
                        return processNode(flow, nextNode, context, session);
                    } else {
                        // If showConversation is true, behave normally: send the LLM response to the user.
                        if (llmResponse != null && session != null && session.isOpen()) {
                            sendMessage(session, llmResponse);
                            logger.info("LLM response sent: {}", llmResponse);
                        }
                    }
                    break;

                case "endNode":
                    logger.info("Reached end node: {}", node.getId());
                    sendMessage(session, "Flow completed at end node: " + node.getId());
                    return null;

                default:
                    logger.warn("Unknown node type encountered: {}", node.getType());
                    sendMessage(session, "Unknown node type: " + node.getType());
                    throw new IllegalArgumentException("Unknown node type: " + node.getType());
            }

            // For nodes that were not auto-chained (like interaction or data nodes, or LLM nodes with showConversation=true)
            // find the next node via global edges.
            Node nextNode = getNextNode(flow, node);
            if (nextNode == null) {
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
