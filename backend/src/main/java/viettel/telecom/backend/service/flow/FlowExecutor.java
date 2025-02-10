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
     * If the node is auto-chained (like a data node or an LLM node with
     * showConversation=false), we immediately process the next node
     * instead of returning its ID.
     */
    public String processNode(Flow flow, Node node, Map<String, Object> context, WebSocketSession session) {
        try {
            logger.debug("processNode() - ID: {}, Type: {}", node.getId(), node.getType());

            switch (node.getType()) {
                case "interactionNode": {
                    // 1) Possibly send a bot response
                    String botResponse = node.getData().getBotResponse();
                    if (botResponse != null && !botResponse.isEmpty()) {
                        sendMessage(session, botResponse);
                        logger.info("Sent bot response: {}", botResponse);
                    }
                    // 2) Capture user response from context, if any
                    String userResponse = (String) context.get("userResponse");
                    if (userResponse != null) {
                        logger.info("User response received: {}", userResponse);
                    }
                    break;
                }

                case "dataNode": {
                    // 1) Process the data node
                    dataHandler.handle(node, context);
                    logger.info("Data node processed: {}", node.getId());

                    // 2) Immediately auto-process the next node
                    Node nextNode = getNextNode(flow, node);
                    if (nextNode == null) {
                        logger.info("No more edges from node: {} => flow ended.", node.getId());
                        sendMessage(session, "Flow ended. No further nodes.");
                        return null;
                    }
                    // Re-enter processNode for chaining
                    return processNode(flow, nextNode, context, session);
                }

                case "llmNode": {
                    // 1) Generate a response
                    llmHandler.handle(node, context, session);
                    String llmResponse = (String) context.get("llmResponse");

                    // 2) Check if showConversation is false => auto-chain
                    Boolean showConversation = (node.getData() != null && node.getData().getShowConversation() != null)
                            ? node.getData().getShowConversation() : Boolean.TRUE;

                    if (Boolean.FALSE.equals(showConversation)) {
                        // Auto-chain to next node
                        context.put("userResponse", llmResponse);
                        logger.info("LLM node (ID: {}) with showConversation=false; auto-chaining to next node. userResponse={}",
                                node.getId(), llmResponse);

                        Node nextNode = getNextNode(flow, node);
                        if (nextNode == null) {
                            logger.info("No more edges from node: {} => flow ended.", node.getId());
                            sendMessage(session, "Flow ended. No further nodes.");
                            return null;
                        }
                        return processNode(flow, nextNode, context, session);
                    } else {
                        // Normal case: send the LLM response to the user
                        if (llmResponse != null && session != null && session.isOpen()) {
                            sendMessage(session, llmResponse);
                            logger.info("LLM response sent: {}", llmResponse);
                        }
                    }
                    break;
                }

                case "endNode": {
                    logger.info("Reached end node: {}", node.getId());
                    sendMessage(session, "Flow completed at end node: " + node.getId());
                    return null;
                }

                default: {
                    logger.warn("Unknown node type encountered: {}", node.getType());
                    sendMessage(session, "Unknown node type: " + node.getType());
                    throw new IllegalArgumentException("Unknown node type: " + node.getType());
                }
            }

            // If we reach here, it means we have a normal or "non-auto-chaining" node.
            // So we just find the next node but do NOT auto-process it. We return the next node ID.
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
