package viettel.telecom.backend.service.flow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import viettel.telecom.backend.entity.flow.Flow;
import viettel.telecom.backend.entity.promptbuilder.Template;
import viettel.telecom.backend.service.llm.LLMService;
import viettel.telecom.backend.service.promptbuilder.TemplateService;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class FlowExecutor {

    private final TemplateService templateService;
    private final LLMService llmService;

    public FlowExecutor(TemplateService templateService, LLMService llmService) {
        this.templateService = templateService;
        this.llmService = llmService;
    }

    public void executeFlow(Flow flow, Map<String, Object> initialContext, WebSocketSession session) {
        Map<String, Object> context = new HashMap<>(initialContext);

        try {
            session.sendMessage(new TextMessage("Starting execution of flow: " + flow.getName()));
            String nextStepId = flow.getSteps().get(0).getId(); // Start with the first step

            while (nextStepId != null) {
                final String currentStepId = nextStepId; // Make it effectively final for use in lambda
                Flow.Step step = flow.getSteps().stream()
                        .filter(s -> s.getId().equals(currentStepId))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Step not found: " + currentStepId));

                session.sendMessage(new TextMessage("Executing step: " + step.getId()));

                switch (step.getActionType()) {
                    case "interaction":
                        nextStepId = handleInteraction(step, context);
                        break;
                    case "llm":
                        nextStepId = handleLLM(step, context);
                        break;
                    case "logic":
                        nextStepId = handleLogic(step, context);
                        break;
                    case "data":
                        nextStepId = handleData(step, context);
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported action type: " + step.getActionType());
                }
            }

            session.sendMessage(new TextMessage("Flow execution completed."));
        } catch (Exception e) {
            try {
                session.sendMessage(new TextMessage("Error during execution: " + e.getMessage()));
            } catch (Exception ignored) {
            }
        } finally {
            try {
                session.close();
            } catch (Exception ignored) {
            }
        }
    }

    private String handleInteraction(Flow.Step step, Map<String, Object> context) {
        String prompt = step.getPrompt();
        if (prompt != null) {
            log.info("Interaction: {}", prompt);
            context.put("userResponse", "Sample response to: " + prompt);
        }
        return step.getNextStepId();
    }

    private String handleLLM(Flow.Step step, Map<String, Object> context) {
        try {
            String templateId = step.getTemplateId();
            Template template = templateService.getTemplate(templateId);
            String systemPrompt = template.getSystemPrompt();

            // Prepare user input
            String userInput = (String) context.get("userResponse");

            // Call LLM service
            @SuppressWarnings("unchecked")
            Map<String, Object> llmConfig = (Map<String, Object>) step.getLlmConfig();
            String modelType = (String) llmConfig.get("modelType");
            Map<String, Object> response = llmService.processRequest(
                    modelType,
                    systemPrompt,
                    userInput,
                    llmConfig
            );

            // Store the LLM response in context
            context.put("llmResponse", response.get("text"));
            log.info("LLM Response: {}", response.get("text"));
        } catch (Exception e) {
            log.error("Error handling LLM: {}", e.getMessage());
        }
        return step.getNextStepId();
    }

    private String handleLogic(Flow.Step step, Map<String, Object> context) {
        String condition = step.getCondition();
        if (condition != null) {
            try {
                SpelExpressionParser parser = new SpelExpressionParser();
                EvaluationContext evalContext = new StandardEvaluationContext(context);
                boolean conditionMet = parser.parseExpression(condition).getValue(evalContext, Boolean.class);

                log.info("Logic evaluated to: {}", conditionMet);
                return conditionMet ? step.getNextStepId() : step.getFallbackStepId();
            } catch (Exception e) {
                log.error("Error evaluating logic: {}", e.getMessage());
            }
        }
        return step.getNextStepId(); // Default to next step if condition fails
    }

    private String handleData(Flow.Step step, Map<String, Object> context) {
        try {
            // Example: Update a value in the context
            @SuppressWarnings("unchecked")
            Map<String, Object> llmConfig = (Map<String, Object>) step.getLlmConfig();
            String key = (String) llmConfig.get("key");
            String value = (String) llmConfig.get("value");
            context.put(key, value);

            log.info("Data operation: Updated context with key '{}' and value '{}'", key, value);
        } catch (Exception e) {
            log.error("Error handling data: {}", e.getMessage());
        }
        return step.getNextStepId();
    }
}
