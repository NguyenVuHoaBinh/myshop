package viettel.telecom.backend.service.llm;

import org.springframework.stereotype.Service;
import viettel.telecom.backend.service.model.ModelHandler;

import java.util.Map;

@Service
public class LLMService {

    private final Map<String, ModelHandler> modelHandlers;

    public LLMService(Map<String, ModelHandler> modelHandlers) {
        this.modelHandlers = modelHandlers;
    }

    public Map<String, Object> processRequest(String modelType, String systemPrompt, String userInput, Map<String, Object> config) {
        // If modelType is null, try to extract it from the nested configuration
        if (modelType == null && config != null && config.get("modelType") != null) {
            modelType = config.get("modelType").toString();
        }
        if (modelType == null) {
            throw new IllegalArgumentException("Model type cannot be null");
        }

        // (Optional) Normalize modelType here (for example, mapping gpt-4o-mini to "openai")
        String key = modelType.toLowerCase();
        // For simplicity, assume your OpenAIHandler is registered under "openai"
        if (key.equals("gpt-4o") || key.equals("gpt-4o-mini") || key.equals("gpt-3o")) {
            key = "openai";
        }

        ModelHandler handler = modelHandlers.get(key);
        if (handler == null) {
            throw new IllegalArgumentException("Unsupported model type: " + modelType);
        }

        System.out.println("Resolved model type: " + key);
        System.out.println("System Prompt: " + systemPrompt);
        System.out.println("User Input: " + userInput);
        System.out.println("Config: " + config);

        Map<String, Object> result = handler.generateResponse(systemPrompt, userInput, config);
        System.out.println("Generated Response: " + result);
        return result;
    }
}
