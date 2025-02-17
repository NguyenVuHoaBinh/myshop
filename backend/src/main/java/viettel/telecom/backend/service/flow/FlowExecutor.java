package viettel.telecom.backend.service.flow;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import viettel.telecom.backend.entity.flow.Flow;
import viettel.telecom.backend.entity.flow.Flow.Node;
import viettel.telecom.backend.service.redis.memory.ChatMemoryService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * A single-step flow executor that processes each node
 * and returns the ID of the next node (or null if flow ends).
 */
@Service
public class FlowExecutor {

    private static final Logger logger = LoggerFactory.getLogger(FlowExecutor.class);

    private final InteractionHandler interactionHandler;
    private final DataHandler dataHandler;
    private final LLMHandler llmHandler;
    private final LogicHandler logicHandler;  // <--- (NEW) we inject this to handle multi-branch logic

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ChatMemoryService chatMemoryService;

    public FlowExecutor(InteractionHandler interactionHandler,
                        DataHandler dataHandler,
                        LLMHandler llmHandler,
                        LogicHandler logicHandler) {
        this.interactionHandler = interactionHandler;
        this.dataHandler = dataHandler;
        this.llmHandler = llmHandler;
        this.logicHandler = logicHandler;
    }

    public String processNode(Flow flow, Node node, Map<String, Object> context, WebSocketSession session) {
        try {
            logger.debug("processNode() - ID: {}, Type: {}", node.getId(), node.getType());

            switch (node.getType()) {
                case "interactionNode": {
                    // Example: send a bot message
                    String botResponse = node.getData().getBotResponse();
                    if (botResponse != null && !botResponse.isEmpty()) {
                        sendMessage(session, botResponse);
                        logger.info("Sent bot response: {}", botResponse);
                    }
                    // Possibly read userResponse from context...
                    break;
                }

                case "dataNode": {
                    dataHandler.handle(node, context, session);
                    String apiResponse = (String) context.get("lastResponse");
                    sendMessage(session, apiResponse);
                    logger.info("Data node processed: {}", node.getId());
                    // Auto-process next node
                    Node nextNode = getNextNode(flow, node);
                    if (nextNode == null) {
                        logger.info("No more edges from node: {} => flow ended.", node.getId());
                        sendMessage(session, "Flow ended. No further nodes.");
                        return null;
                    }
                    return processNode(flow, nextNode, context, session);
                }

                case "llmNode": {
                    llmHandler.handle(node, context, session);
                    String llmResponse = (String) context.get("llmResponse");
                    chatMemoryService.storeUserChat(session.getId(), "assistant", llmResponse);

                    // If showConversation=false, auto-chain to next
                    Boolean showConversation = node.getData().getShowConversation();
                    if (showConversation != null && !showConversation) {
                        context.put("userResponse", llmResponse);
                        Node nextNode = getNextNode(flow, node);
                        if (nextNode == null) {
                            logger.info("No more edges => flow ended.");
                            sendMessage(session, "Flow ended. No further nodes.");
                            return null;
                        }
                        return processNode(flow, nextNode, context, session);
                    } else {
                        // Otherwise, just send the response
                        if (llmResponse != null && session != null && session.isOpen()) {
                            sendMessage(session, llmResponse);
                            logger.info("LLM response sent: {}", llmResponse);
                        }
                    }
                    break;
                }

                // (NEW) MULTI-BRANCH LOGIC NODE:
                case "logicNode": {
                    // Evaluate expressions via LogicHandler
                    String nextNodeId = logicHandler.handle(node, context);

                    if (nextNodeId == null) {
                        // No match => flow ends or fallback
                        logger.info("No match in logic node => using node.next or ending flow: {}", node.getId());
                        sendMessage(session, "Flow ended or fallback not found for logic node.");
                        return null;
                    }
                    Node nextNode = getNodeById(flow, nextNodeId);
                    if (nextNode == null) {
                        logger.warn("No node found with ID {} => flow ended.", nextNodeId);
                        sendMessage(session, "Flow ended. Next node not found: " + nextNodeId);
                        return null;
                    }
                    logger.info("Logic node matched => jumping to node: {}", nextNodeId);
                    return processNode(flow, nextNode, context, session);
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

            // If we reach here, handle next node but don't auto-process
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
     * Find the next node by looking at the first edge with source == currentNode.id
     */
    private Node getNextNode(Flow flow, Node currentNode) {
        return flow.getEdges().stream()
                .filter(edge -> edge.getSource().equals(currentNode.getId()))
                .map(edge -> getNodeById(flow, edge.getTarget()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Utility method to find a node by ID in the flow
     */
    private Node getNodeById(Flow flow, String nodeId) {
        return flow.getNodes().stream()
                .filter(n -> n.getId().equals(nodeId))
                .findFirst()
                .orElse(null);
    }

    private void sendMessage(WebSocketSession session, String message) {
        if (session == null || !session.isOpen()) {
            logger.debug("No open session; cannot send message: {}", message);
            return;
        }
        try {
            chatMemoryService.storeUserChat(session.getId(), "assistant", message);

            Map<String, String> response = new HashMap<>();
            response.put("sender", "bot");
            response.put("message", message);

            String jsonResponse = objectMapper.writeValueAsString(response);
            session.sendMessage(new TextMessage(jsonResponse));
        } catch (IOException e) {
            logger.error("Failed to send WebSocket message: {}", e.getMessage(), e);
        }
    }
}
