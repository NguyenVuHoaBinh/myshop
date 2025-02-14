package viettel.telecom.backend.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import viettel.telecom.backend.entity.promptbuilder.Template;
import viettel.telecom.backend.service.llm.LLMService;
import viettel.telecom.backend.service.promptbuilder.TemplateService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for auto system prompting tasks:
 * 1) Check existing Template systemPrompt
 * 2) Decide whether to use "create" or "edit" designated prompt
 * 3) Send to LLM (DeepSeek) with default parameters
 */
@RestController
@RequestMapping("/api/auto-system-prompt")
@Slf4j
public class AutoSystemPromptingController {

    private final LLMService llmService;
    private final TemplateService templateService;

    // Inject text files from resources
    @Value("classpath:templates/create_new.txt")
    private Resource createNewPromptResource;

    @Value("classpath:templates/edit.txt")
    private Resource editPromptResource;

    public AutoSystemPromptingController(LLMService llmService,
                                         TemplateService templateService) {
        this.llmService = llmService;
        this.templateService = templateService;
    }

    /**
     * Example endpoint that:
     * 1) Fetches the template by ID
     * 2) Checks systemPrompt
     * 3) If empty => uses "create_new" designated prompt
     *    Else => uses "edit" designated prompt
     * 4) Sends to DeepSeek
     */
    @PostMapping("/{templateId}/generate")
    public ResponseEntity<Map<String, Object>> generateSystemPrompt(
            @PathVariable String templateId,
            @RequestParam(required = false, defaultValue = "") String userInput
    ) throws IOException {

        // 1) Fetch the Template
        Template template = templateService.getTemplate(templateId);
        String existingSystemPrompt = template.getSystemPrompt();

        // 2) Decide which designated prompt file to load
        String designatedPrompt;
        if (existingSystemPrompt == null || existingSystemPrompt.trim().isEmpty()) {
            designatedPrompt = readResourceFile(createNewPromptResource);
            log.info("Using CREATE_NEW prompt for templateId={}", templateId);
        } else {
            designatedPrompt = readResourceFile(editPromptResource);
            log.info("Using EDIT prompt for templateId={}", templateId);
        }

        // 3) Prepare default config for LLM
        //    modelType or "deepseek" is recognized by your LLMService
        Map<String, Object> config = new HashMap<>();
        config.put("modelType", "deepseek-reasoner");
        // You can set any other default params here (temperature, max_tokens, etc.)
        config.put("temperature", 0);
        config.put("max_tokens", 1024);
        config.put("stream", false);  // If you want streaming, set true and handle differently

        // 4) Send to LLM (DeepSeekHandler) using LLMService
        Map<String, Object> llmResponse = llmService.processRequest(
                "deepseek",         // or could be null and rely on modelType from config
                designatedPrompt,   // systemPrompt
                userInput,          // userInput from request param
                config
        );

        // Return response from DeepSeek
        return ResponseEntity.ok(llmResponse);
    }

    /**
     * Utility method to read text from a resource file.
     */
    private String readResourceFile(Resource resource) throws IOException {
        byte[] bytes = resource.getInputStream().readAllBytes();
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
