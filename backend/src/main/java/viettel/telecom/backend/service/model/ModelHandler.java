package viettel.telecom.backend.service.model;


import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface ModelHandler {
    Map<String, Object> generateResponse(String systemPrompt, String userInput, Map<String, Object> config);
}