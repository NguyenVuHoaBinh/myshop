package viettel.telecom.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import viettel.telecom.backend.entity.promptbuilder.Template;
import viettel.telecom.backend.service.promptbuilder.TemplateService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/templates")
public class TemplateController {

    @Autowired
    private TemplateService templateService;

    @PostMapping
    public Template createTemplate(@Valid @RequestBody Template template) {
        return templateService.createTemplate(template);
    }

    @PutMapping("/{id}")
    public Template updateTemplate(@PathVariable String id, @Valid @RequestBody Template template) {
        return templateService.updateTemplate(id, template);
    }

    @GetMapping("/{id}")
    public Template getTemplate(@PathVariable String id) {
        return templateService.getTemplate(id);
    }

    @GetMapping
    public Page<Template> getAllTemplates(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        return templateService.getAllTemplates(page, size, sortBy, sortDir);
    }

    @DeleteMapping("/{id}")
    public void deleteTemplate(@PathVariable String id) {
        templateService.deleteTemplate(id);
    }
}
