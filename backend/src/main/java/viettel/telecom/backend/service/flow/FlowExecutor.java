package viettel.telecom.backend.service.flow;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import viettel.telecom.backend.entity.flow.Flow;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class FlowExecutor {

    private final InteractionHandler interactionHandler;
    private final DataHandler dataHandler;
    private final LLMHandler llmHandler;

    public FlowExecutor(InteractionHandler interactionHandler, DataHandler dataHandler, LLMHandler llmHandler) {
        this.interactionHandler = interactionHandler;
        this.dataHandler = dataHandler;
        this.llmHandler = llmHandler;
    }

    public void executeFlow(Flow flow, Map<String, Object> initialContext, WebSocketSession session) {
        Map<String, Object> context = new HashMap<>(initialContext);

        try {
            sendMessage(session, "Starting flow: " + flow.getName());

            Flow.Node currentNode = flow.getNodes().stream()
                    .filter(node -> "startNode".equals(node.getType()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Start node not found"));

            while (currentNode != null) {
                sendMessage(session, "Processing node: " + currentNode.getId());
                sendMessage(session, "Node type: " + currentNode.getType());

                try {
                    switch (currentNode.getType()) {
                        case "startNode":
                            sendMessage(session, "Start node encountered. Initializing flow...");
                            break; // Continue to the next node
                        case "interactionNode":
                            sendMessage(session, currentNode.getData().getBotResponse());
                            String userResponse = (String) context.get("userResponse");
                            context.put("userResponse", userResponse);
                            break;
                        case "dataNode":
                            dataHandler.handle(currentNode, context);
                            break;
                        case "llmNode":
                            llmHandler.handle(currentNode, context, session);
                            break;
                        case "endNode":
                            sendMessage(session, "Flow execution completed.");
                            currentNode = null;
                            continue; // Exit the loop
                        default:
                            sendMessage(session, "Unknown node type encountered: " + currentNode.getType());
                            throw new IllegalArgumentException("Unknown node type: " + currentNode.getType());
                    }
                } catch (Exception nodeProcessingException) {
                    sendMessage(session, "Error while processing node: " + currentNode.getId() + ". Details: " + nodeProcessingException.getMessage());
                    throw nodeProcessingException;
                }

                // Transition to the next node
                sendMessage(session, "Transitioning from node: " + currentNode.getId());
                Flow.Node nextNode = getNextNode(flow, currentNode);
                if (nextNode == null) {
                    sendMessage(session, "Error: No valid next node found. Ending flow execution.");
                    break;
                }

                sendMessage(session, "Next node found: " + nextNode.getId());
                currentNode = nextNode;
            }
        } catch (Exception e) {
            sendMessage(session, "Error during execution: " + e.getMessage());
        }
    }



    private Flow.Node getNodeById(Flow flow, String nodeId) {
        return flow.getNodes().stream()
                .filter(node -> node.getId().equals(nodeId))
                .findFirst()
                .orElse(null);
    }

    private Flow.Node getNextNode(Flow flow, Flow.Node currentNode) {
        return flow.getEdges().stream()
                .filter(edge -> edge.getSource().equals(currentNode.getId()))
                .map(edge -> getNodeById(flow, edge.getTarget()))
                .findFirst()
                .orElse(null);
    }

    private void sendMessage(WebSocketSession session, String message) {
        if (session == null) {
            System.out.println("WebSocketSession is null. Message: " + message);
            return;
        }

        try {
            session.sendMessage(new TextMessage(message));
        } catch (IOException e) {
            System.err.println("Failed to send WebSocket message: " + e.getMessage());
        }
    }

    public String processNode(Flow flow, Flow.Node node, Map<String, Object> context, WebSocketSession session) {
        try {
            switch (node.getType()) {
                case "startNode":
                    // Start the flow; typically doesn't require processing
                    sendMessage(session, "Start node encountered. Initializing flow...");
                    break; // Continue to the next node
                case "interactionNode":
                    sendMessage(session, node.getData().getBotResponse());
                    String userResponse = (String) context.get("userResponse");
                    context.put("userResponse", userResponse);
                    break;
                case "dataNode":
                    dataHandler.handle(node, context);
                    break;
                case "llmNode":
                    llmHandler.handle(node, context, session);
                    break;
                case "endNode":
                    sendMessage(session, "Flow execution completed.");
                    return null; // End of flow
                default:
                    sendMessage(session, "Unknown node type encountered: " + node.getType());
                    throw new IllegalArgumentException("Unknown node type: " + node.getType());
            }

            // Get the next node
            Flow.Node nextNode = getNextNode(flow, node);
            return nextNode != null ? nextNode.getId() : null;
        } catch (Exception e) {
            throw new RuntimeException("Error processing node: " + e.getMessage(), e);
        }
    }



}
