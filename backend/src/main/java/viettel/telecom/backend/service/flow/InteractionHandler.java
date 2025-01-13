package viettel.telecom.backend.service.flow;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import viettel.telecom.backend.entity.flow.Flow;
import viettel.telecom.backend.service.logging.LogManagementService;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.*;

@Service
public class InteractionHandler {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final LogManagementService logManagementService;

    public InteractionHandler(LogManagementService logManagementService) {
        this.logManagementService = logManagementService;
    }

    public String handle(Flow.Step step, Map<String, Object> context, WebSocketSession session) {
        int retryCount = 0;
        int maxRetries = 3;

        while (retryCount < maxRetries) {
            try {
                // Personalize the prompt using context
                String personalizedPrompt = personalizePrompt(step.getPrompt(), context);
                sendMessage(session, personalizedPrompt);

                // Wait for the user's response
                Future<String> userResponseFuture = scheduler.submit(() -> {
                    while (!context.containsKey("userResponse")) {
                        Thread.sleep(500); // Poll for user response
                    }
                    return (String) context.get("userResponse");
                });

                String userResponse = userResponseFuture.get(step.getTimeout(), TimeUnit.SECONDS);

                // Validate the user response
                if (step.getValidationRules() != null && !userResponse.matches(step.getValidationRules())) {
                    retryCount++;
                    sendMessage(session, "Invalid input. Please try again.");
                    logManagementService.writeLog("INFO", "Invalid input detected. Retry count: " + retryCount);
                    continue; // Retry the current step
                }

                // Store the user response in context and return the next step
                context.put("userResponse", userResponse);
                return step.getNextStepId();

            } catch (TimeoutException e) {
                sendMessage(session, "Timeout occurred. Proceeding with fallback.");
                logManagementService.writeLog("ERROR", "Timeout occurred for step: " + step.getId());
                return step.getFallbackStepId();

            } catch (Exception e) {
                sendMessage(session, "Error: " + e.getMessage());
                logManagementService.writeLog("ERROR", "Exception during interaction: " + e.getMessage());
                return step.getFallbackStepId();
            }
        }

        // Exceeded max retries
        sendMessage(session, "Maximum retry attempts reached. Proceeding with fallback.");
        logManagementService.writeLog("ERROR", "Max retries reached for step: " + step.getId());
        return step.getFallbackStepId();
    }

    /**
     * Sends a message through the WebSocket session.
     *
     * @param session The WebSocket session.
     * @param message The message to send.
     */
    private void sendMessage(WebSocketSession session, String message) {
        try {
            session.sendMessage(new TextMessage(message));
        } catch (IOException e) {
            logManagementService.writeLog("ERROR", "Failed to send WebSocket message: " + e.getMessage());
        }
    }

    /**
     * Personalizes the prompt using context variables.
     *
     * @param prompt  The original prompt.
     * @param context The context containing personalization data.
     * @return The personalized prompt.
     */
    private String personalizePrompt(String prompt, Map<String, Object> context) {
        for (Map.Entry<String, Object> entry : context.entrySet()) {
            prompt = prompt.replace("{" + entry.getKey() + "}", entry.getValue().toString());
        }
        return prompt;
    }
}
