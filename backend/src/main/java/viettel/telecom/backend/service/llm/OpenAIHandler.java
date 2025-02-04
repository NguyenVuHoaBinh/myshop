package viettel.telecom.backend.service.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import viettel.telecom.backend.service.model.ModelHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("openai")
public class OpenAIHandler implements ModelHandler {

    private final RestTemplate restTemplate;
    private final WebClient webClient;
    private final String apiUrl;
    private final String apiKey;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OpenAIHandler(RestTemplate restTemplate,
                         WebClient.Builder webClientBuilder,
                         @Value("${openai.api.url}") String apiUrl,
                         @Value("${openai.api.key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.webClient = webClientBuilder
                .baseUrl(apiUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();
    }

    @Override
    public Map<String, Object> generateResponse(String systemPrompt, String userInput, Map<String, Object> config) {
        Map<String, Object> requestBody = buildRequestBody(systemPrompt, userInput, config);
        boolean stream = (boolean) config.getOrDefault("stream", false);

        if (stream) {
            // --- Streaming Mode using WebClient ---
            StringBuilder responseBuilder = new StringBuilder();
            // The API returns a stream of chunks (each is a JSON object prefixed by "data:")
            webClient.post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToFlux(String.class)
                    .toStream()
                    .forEach(chunk -> {
                        String trimmed = chunk.trim();
                        // Skip "[DONE]" messages
                        if (trimmed.equals("[DONE]")) {
                            return;
                        }
                        // Remove any "data:" prefix
                        String data = trimmed.startsWith("data:") ? trimmed.substring(5).trim() : trimmed;
                        if (!data.isEmpty()) {
                            try {
                                Map<String, Object> chunkMap = objectMapper.readValue(data, Map.class);
                                // Check if this chunk contains a "choices" array
                                if (chunkMap.containsKey("choices")) {
                                    List<Map<String, Object>> choices = (List<Map<String, Object>>) chunkMap.get("choices");
                                    if (choices != null && !choices.isEmpty()) {
                                        Map<String, Object> choice = choices.get(0);
                                        // In streaming mode, OpenAI sends "delta" objects
                                        if (choice.containsKey("delta")) {
                                            Map<String, Object> delta = (Map<String, Object>) choice.get("delta");
                                            if (delta != null && delta.get("content") != null) {
                                                responseBuilder.append(delta.get("content").toString());
                                            }
                                        }
                                        // Fallback: if no "delta", try to get "message"
                                        else if (choice.containsKey("message")) {
                                            Map<String, Object> messageMap = (Map<String, Object>) choice.get("message");
                                            if (messageMap != null && messageMap.get("content") != null) {
                                                responseBuilder.append(messageMap.get("content").toString());
                                            }
                                        }
                                    }
                                }
                            } catch (IOException e) {
                                // Log and ignore individual chunk errors
                                e.printStackTrace();
                            }
                        }
                    });
            // Build a result map that mimics a non-streaming response structure.
            Map<String, Object> result = new HashMap<>();
            // Option 1: Put the accumulated text under a key "content"
            result.put("content", responseBuilder.toString());
            // Option 2: Mimic the "choices" structure:
            result.put("choices", List.of(Map.of("message", Map.of("content", responseBuilder.toString()))));
            return result;
        } else {
            // --- Non-Streaming Mode using RestTemplate ---
            // Force stream to false in the payload.
            requestBody.put("stream", false);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity, Map.class);
            return response.getBody();
        }
    }

    private Map<String, Object> buildRequestBody(String systemPrompt, String userInput, Map<String, Object> config) {
        Map<String, Object> requestBody = new HashMap<>();
        // Use the provided aiModel or default to "gpt-4"
        requestBody.put("model", config.getOrDefault("aiModel", "gpt-4"));
        requestBody.put("temperature", config.getOrDefault("temperature", 0.7));
        requestBody.put("max_tokens", config.getOrDefault("max_tokens", 100));
        requestBody.put("stream", config.getOrDefault("stream", false));
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userInput)
        ));
        return requestBody;
    }
}
