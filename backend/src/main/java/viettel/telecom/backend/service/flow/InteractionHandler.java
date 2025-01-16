package viettel.telecom.backend.service.flow;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import viettel.telecom.backend.entity.flow.Flow;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class InteractionHandler {

    /**
     * Asynchronous handling of interaction nodes.
     * Returns a CompletableFuture that waits for user input asynchronously.
     */
    public CompletableFuture<String> handleAsync(Flow.Node node, Map<String, Object> context, WebSocketSession session) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Send bot response via WebSocket
                sendMessage(session, node.getData().getBotResponse());

                // Simulate waiting for user input
                String userResponse;
                synchronized (context) {
                    while (!context.containsKey("userResponse")) {
                        context.wait(500); // Wait for 500ms intervals until input is present
                    }
                    userResponse = (String) context.get("userResponse");
                }

                // Validate user response
                if (node.getData().getBotResponse() != null && !userResponse.matches(node.getData().getBotResponse())) {
                    sendMessage(session, "Invalid input. Please try again.");
                    return null; // Indicate invalid response
                }

                // Store valid response in context
                context.put("userResponse", userResponse);
                return userResponse;

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interaction handling interrupted", e);
            } catch (Exception e) {
                throw new RuntimeException("Error in handleAsync: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Synchronous handling of interaction nodes.
     * Blocks until user input is received.
     */
    public String handle(Flow.Node node, Map<String, Object> context, WebSocketSession session) {
        try {
            // Block on the asynchronous method and return the result
            return handleAsync(node, context, session).get();
        } catch (Exception e) {
            throw new RuntimeException("Error handling interaction node: " + e.getMessage(), e);
        }
    }

    /**
     * Utility method for sending messages via WebSocket.
     */
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
}
