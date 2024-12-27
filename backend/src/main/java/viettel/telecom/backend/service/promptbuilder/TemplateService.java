package viettel.telecom.backend.service.promptbuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import viettel.telecom.backend.entity.promptbuilder.Template;
import viettel.telecom.backend.repository.promptbuilder.TemplateRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TemplateService {

    @Autowired
    private TemplateRepository templateRepository;

    public Template createTemplate(Template template) {
        template.setCreatedAt(LocalDate.now());
        template.setUpdatedAt(LocalDate.now());
        return templateRepository.save(template);
    }

    public Template updateTemplate(String id, Template updatedTemplate) {
        Template existingTemplate = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found"));

        updatedTemplate.setId(existingTemplate.getId());
        updatedTemplate.setCreatedAt(existingTemplate.getCreatedAt()); // Preserve original createdAt
        updatedTemplate.setUpdatedAt(LocalDate.now()); // Update only updatedAt
        return templateRepository.save(updatedTemplate);
    }


    public Template getTemplate(String id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found"));
    }

    public Page<Template> getAllTemplates(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        return templateRepository.findAll(pageRequest);
    }

    public void deleteTemplate(String id) {
        templateRepository.deleteById(id);
    }

    public List<Template> getAllTemplates() {
        return (List<Template>) templateRepository.findAll();
    }
}