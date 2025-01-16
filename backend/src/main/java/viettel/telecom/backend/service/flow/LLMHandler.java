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

    public String handle(Flow.Node node, Map<String, Object> context, WebSocketSession session) {
        try {
            // Fetch the template details from the node
            String templateId = node.getData().getTemplateId();
            var template = templateService.getTemplate(templateId);

            // Extract the system prompt from the template
            String systemPrompt = template.getSystemPrompt();

            // Get user input from context
            String userInput = (String) context.getOrDefault("userResponse", "");

            // Retrieve LLM-specific configuration from the node
            Map<String, Object> llmConfig = (Map<String, Object>) node.getData().getSelectedTemplate();

            // Process the LLM request
            Map<String, Object> response = llmService.processRequest(
                    (String) llmConfig.get("aiModel"),
                    systemPrompt,
                    userInput,
                    llmConfig
            );

            // Save the LLM response in the context
            context.put("llmResponse", response.get("text"));

            // Return the next step ID
            return node.getData().getTemplateId();
        } catch (Exception e) {
            // Return the fallback step ID in case of errors
            return node.getData().getLabel();
        }
    }
}