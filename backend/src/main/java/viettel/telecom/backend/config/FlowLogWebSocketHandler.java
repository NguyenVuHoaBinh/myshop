package viettel.telecom.backend.config;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FlowLogWebSocketHandler extends TextWebSocketHandler {

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        executorService.submit(() -> {
            try {
                session.sendMessage(new TextMessage("Connection established. Streaming logs..."));

                // Simulate step-by-step execution logs
                for (int i = 1; i <= 3; i++) {
                    session.sendMessage(new TextMessage("Executing step " + i));
                    Thread.sleep(1000); // Simulated delay
                }

                session.sendMessage(new TextMessage("Flow execution completed."));
                session.close();
            } catch (Exception e) {
                try {
                    session.sendMessage(new TextMessage("Error during execution: " + e.getMessage()));
                    session.close();
                } catch (Exception ignored) {
                }
            }
        });
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Optionally process client messages if needed (for bidirectional communication)
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        session.close();
    }
}