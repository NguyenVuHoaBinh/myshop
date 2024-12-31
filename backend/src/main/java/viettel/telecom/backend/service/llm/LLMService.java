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
        System.out.println("Model Type: " + modelType);
        System.out.println("System Prompt: " + systemPrompt);
        System.out.println("User Input: " + userInput);
        System.out.println("Config: " + config);

        ModelHandler handler = modelHandlers.get(modelType.toLowerCase());
        if (handler == null) {
            throw new IllegalArgumentException("Unsupported model type: " + modelType);
        }

        Map<String, Object> result = handler.generateResponse(systemPrompt, userInput, config);

        System.out.println("Generated Response: " + result);
        return result;
    }

}