package viettel.telecom.backend.service.flow;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import viettel.telecom.backend.entity.flow.Flow;

import java.util.HashMap;
import java.util.Map;

@Service
public class FlowExecutor {

    private final InteractionHandler interactionHandler;
    private final LLMHandler llmHandler;
    private final LogicHandler logicHandler;
    private final DataHandler dataHandler;

    public FlowExecutor(InteractionHandler interactionHandler,
                        LLMHandler llmHandler,
                        LogicHandler logicHandler,
                        DataHandler dataHandler) {
        this.interactionHandler = interactionHandler;
        this.llmHandler = llmHandler;
        this.logicHandler = logicHandler;
        this.dataHandler = dataHandler;
    }

    public void executeFlow(String flowId, Map<String, Object> initialContext, WebSocketSession session) {
        Map<String, Object> context = new HashMap<>(initialContext);

        try {
            // Fetch flow details (mocked for simplicity, replace with real fetch logic)
            Flow flow = getFlowById(flowId);

            session.sendMessage(new TextMessage("Starting flow: " + flow.getName()));

            String nextStepId = flow.getSteps().get(0).getId();
            while (nextStepId != null) {
                final String currentStepId = nextStepId;
                Flow.Step step = flow.getSteps().stream()
                        .filter(s -> s.getId().equals(currentStepId))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Step not found: " + currentStepId));

                switch (step.getActionType()) {
                    case "interaction":
                        nextStepId = interactionHandler.handle(step, context, session);
                        break;
                    case "llm":
                        nextStepId = llmHandler.handle(step, context, session);
                        break;
                    case "logic":
                        nextStepId = logicHandler.handle(step, context);
                        break;
                    case "data":
                        nextStepId = dataHandler.handle(step, context);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown action type: " + step.getActionType());
                }
            }
            session.sendMessage(new TextMessage("Flow execution completed."));
        } catch (Exception e) {
            try {
                session.sendMessage(new TextMessage("Error during execution: " + e.getMessage()));
            } catch (Exception ignored) {}
        }
    }

    private Flow getFlowById(String flowId) {
        // Mocked flow for demonstration. Replace with actual database/service call.
        return new Flow();
    }
}
