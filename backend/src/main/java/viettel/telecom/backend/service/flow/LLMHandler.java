package viettel.telecom.backend.service.flow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import viettel.telecom.backend.entity.flow.Flow;
import viettel.telecom.backend.service.llm.LLMService;
import viettel.telecom.backend.service.promptbuilder.TemplateService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
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
            log.debug("Handling LLM node with ID: {}", node.getId());

            // Fetch the template details from the node
            String templateId = node.getData().getTemplateId();
            log.debug("Fetching template with ID: {}", templateId);
            var template = templateService.getTemplate(templateId);

            // Extract the system prompt from the template
            String systemPrompt = template.getSystemPrompt();
            log.debug("System prompt: {}", systemPrompt);

            // Get user input from context
            String userInput = (String) context.getOrDefault("userResponse", "");
            log.debug("User input: {}", userInput);

            // Retrieve LLM-specific configuration from the SelectedTemplate
            Flow.Node.NodeData.SelectedTemplate selectedTemplate = node.getData().getSelectedTemplate();
            log.debug("Selected Template ID: {}, Name: {}", selectedTemplate.getId(), selectedTemplate.getName());

            // Extract AI model and other relevant fields from SelectedTemplate
            String aiModel = selectedTemplate.getAiModel();
            log.debug("AI Model: {}", aiModel);

            // Prepare configuration map for LLM processing
            Map<String, Object> llmConfig = new HashMap<>();
            llmConfig.put("fields", selectedTemplate.getFields());
            llmConfig.put("description", selectedTemplate.getDescription());

            // Process the LLM request
            Map<String, Object> response = llmService.processRequest(
                    aiModel,
                    systemPrompt,
                    userInput,
                    llmConfig
            );

            // Correctly extract the content from the response
            Map<String, Object> firstChoice = ((List<Map<String, Object>>) response.get("choices")).get(0);
            Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
            String llmResponse = (String) message.get("content");

            // Save the LLM response in the context
            context.put("llmResponse", llmResponse);
            log.debug("LLM response: {}", llmResponse);

            // Return the next step ID
            return node.getData().getTemplateId();
        } catch (Exception e) {
            log.error("Error handling LLM node: {}", e.getMessage(), e);
            // Return the fallback step ID in case of errors
            return node.getData().getLabel();
        }
    }




}