package viettel.telecom.backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/model-types")
public class ModelController {

    private final List<String> modelTypes;

    // If using application.yml/properties, inject via @Value or @ConfigurationProperties
    public ModelController(
            @Value("${app.llm.model-types}") List<String> modelTypes
    ) {
        this.modelTypes = modelTypes;
    }

    @GetMapping
    public List<String> getModelTypes() {
        return modelTypes;
    }
}
