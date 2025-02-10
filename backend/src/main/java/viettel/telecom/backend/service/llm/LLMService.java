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

        // Use the enum to resolve the model type to its canonical key
        String resolvedKey = ModelType.resolve(modelType);
        if (resolvedKey == null) {
            throw new IllegalArgumentException("Unsupported model type: " + modelType);
        }

        // Retrieve the appropriate handler based on the canonical key
        ModelHandler handler = modelHandlers.get(resolvedKey);
        if (handler == null) {
            throw new IllegalArgumentException("No handler found for model type: " + resolvedKey);
        }

        System.out.println("Resolved model type: " + resolvedKey);
        System.out.println("System Prompt: " + systemPrompt);
        System.out.println("User Input: " + userInput);
        System.out.println("Config: " + config);

        Map<String, Object> result = handler.generateResponse(systemPrompt, userInput, config);
        System.out.println("Generated Response: " + result);
        return result;
    }
}
