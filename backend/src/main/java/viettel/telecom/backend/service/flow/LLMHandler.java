package viettel.telecom.backend.service.flow;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import viettel.telecom.backend.entity.flow.Flow;
import viettel.telecom.backend.service.llm.LLMService;
import viettel.telecom.backend.service.promptbuilder.TemplateService;

import java.util.Map;

@Service
public class LLMHandler {

    private final LLMService llmService;
    private final TemplateService templateService;

    public LLMHandler(LLMService llmService, TemplateService templateService) {
        this.llmService = llmService;
        this.templateService = templateService;
    }

    public String handle(Flow.Step step, Map<String, Object> context, WebSocketSession session) {
        try {
            String templateId = step.getTemplateId();
            var template = templateService.getTemplate(templateId);
            String systemPrompt = template.getSystemPrompt();

            String userInput = (String) context.getOrDefault("userResponse", "");
            Map<String, Object> llmConfig = (Map<String, Object>) step.getLlmConfig();

            Map<String, Object> response = llmService.processRequest(
                    (String) llmConfig.get("modelType"),
                    systemPrompt,
                    userInput,
                    llmConfig
            );

            context.put("llmResponse", response.get("text")); // Storing LLM response in context
            return step.getNextStepId();
        } catch (Exception e) {
            return step.getFallbackStepId(); // Move to fallback step on failure
        }
    }
}
