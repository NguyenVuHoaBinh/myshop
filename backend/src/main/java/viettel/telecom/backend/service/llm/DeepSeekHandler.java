package viettel.telecom.backend.service.llm;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import viettel.telecom.backend.service.model.ModelHandler;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("deepseek")
public class DeepSeekHandler implements ModelHandler {

    private final RestTemplate restTemplate;
    private final String apiUrl;
    private final String apiKey;

    public DeepSeekHandler(RestTemplate restTemplate,
                           @Value("${deepseek.api.url}") String apiUrl,
                           @Value("${deepseek.api.key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
    }

    @Override
    public Map<String, Object> generateResponse(String systemPrompt, String userInput, Map<String, Object> config) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey); // Verify if DeepSeek uses Bearer token auth

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", config.getOrDefault("model", "deepseek-chat"));
        requestBody.put("temperature", config.getOrDefault("temperature", 0.7));
        requestBody.put("max_tokens", config.getOrDefault("max_tokens", 100));
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userInput)
        ));

        // Add any DeepSeek-specific parameters here
        // requestBody.put("stream", config.getOrDefault("stream", false));

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity, Map.class);
        return response.getBody();
    }
}