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

            // Fetch the template details from the node using its templateId.
            String templateId = node.getData().getTemplateId();
            log.debug("Fetching template with ID: {}", templateId);
            var template = templateService.getTemplate(templateId);

            // Extract the system prompt from the fetched template.
            String systemPrompt = template.getSystemPrompt();
            log.debug("System prompt: {}", systemPrompt);

            // Retrieve the user input from the execution context.
            String userInput = (String) context.getOrDefault("userResponse", "");
            log.debug("User input: {}", userInput);

            // Retrieve the LLM configuration from the node's data.
            var llmConfigData = node.getData().getLlmconfig();
            if (llmConfigData == null) {
                log.warn("LLM configuration is missing in node data. Using default configuration.");
                // Create a default configuration.
                llmConfigData = new Flow.Node.NodeData.LLMConfig();
                llmConfigData.setAiModel("gpt-4o");      // default AI model
                llmConfigData.setTemperature(0.7);       // default temperature
                llmConfigData.setMax_tokens(100);        // default maximum tokens
                llmConfigData.setStream(false);          // default: non-streaming
            }
            String aiModel = llmConfigData.getAiModel();
            log.debug("AI Model from LLMConfig: {}", aiModel);

            // Build a configuration map for LLM processing using values from llmConfigData.
            Map<String, Object> config = new HashMap<>();
            config.put("aiModel", aiModel);
            config.put("temperature", llmConfigData.getTemperature());
            config.put("max_tokens", llmConfigData.getMax_tokens());
            config.put("stream", llmConfigData.getStream());

            // Optionally, you can also pass selectedTemplate details if needed:
            var selectedTemplate = node.getData().getSelectedTemplate();
            if (selectedTemplate != null) {
                config.put("selectedTemplateFields", selectedTemplate.getFields());
                config.put("selectedTemplateDescription", selectedTemplate.getDescription());
            }

            // Process the LLM request using the provided configuration.
            Map<String, Object> response = llmService.processRequest(
                    aiModel,
                    systemPrompt,
                    userInput,
                    config
            );

            // Extract the content from the response.
            String llmResponse = "";
            if (response != null && response.get("choices") != null) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> firstChoice = choices.get(0);
                    Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                    if (messageMap != null && messageMap.get("content") != null) {
                        llmResponse = (String) messageMap.get("content");
                    }
                }
            }

            // Save the LLM response in the context for further processing.
            context.put("llmResponse", llmResponse);
            log.debug("LLM response: {}", llmResponse);

            // Return the next step identifier (here we simply return the templateId as a placeholder).
            return node.getData().getTemplateId();
        } catch (Exception e) {
            log.error("Error handling LLM node: {}", e.getMessage(), e);
            // In case of error, return a fallback value (e.g. node label).
            return node.getData().getLabel();
        }
    }
}
