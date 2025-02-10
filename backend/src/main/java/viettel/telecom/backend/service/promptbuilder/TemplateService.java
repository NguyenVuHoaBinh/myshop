package viettel.telecom.backend.service.promptbuilder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import viettel.telecom.backend.entity.promptbuilder.Template;
import viettel.telecom.backend.exception.TemplateNotFoundException;
import viettel.telecom.backend.repository.promptbuilder.TemplateRepository;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class TemplateService {

    @Autowired
    private TemplateRepository templateRepository;

    public Template createTemplate(Template template) {
        log.info("Creating template with name: {}", template.getName());
        template.setCreatedAt(LocalDate.now());
        template.setUpdatedAt(LocalDate.now());
        Template savedTemplate = templateRepository.save(template);
        log.info("Template created with ID: {}", savedTemplate.getId());
        return savedTemplate;
    }

    public Template updateTemplate(String id, Template updatedTemplate) {
        log.info("Updating template with ID: {}", id);
        Template existingTemplate = templateRepository.findById(id)
                .orElseThrow(() -> new TemplateNotFoundException("Template with ID " + id + " not found"));

        updatedTemplate.setId(existingTemplate.getId());
        updatedTemplate.setCreatedAt(existingTemplate.getCreatedAt()); // Preserve original createdAt
        updatedTemplate.setUpdatedAt(LocalDate.now()); // Update only updatedAt
        Template savedTemplate = templateRepository.save(updatedTemplate);
        log.info("Template updated with ID: {}", savedTemplate.getId());
        return savedTemplate;
    }

    public Template getTemplate(String id) {
        log.info("Fetching template with ID: {}", id);
        return templateRepository.findById(id)
                .orElseThrow(() -> new TemplateNotFoundException("Template with ID " + id + " not found"));
    }

    public void deleteTemplate(String id) {
        log.info("Deleting template with ID: {}", id);
        if (!templateRepository.existsById(id)) {
            throw new TemplateNotFoundException("Template with ID " + id + " not found");
        }
        templateRepository.deleteById(id);
        log.info("Template deleted with ID: {}", id);
    }

    public Page<Template> getAllTemplates(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        return templateRepository.findAll(pageRequest);
    }
    public List<Template> bulkInsertTemplates(List<Template> templates) {
        log.info("Bulk inserting {} templates", templates.size());
        templates.forEach(template -> {
            template.setCreatedAt(LocalDate.now());
            template.setUpdatedAt(LocalDate.now());
        });
        List<Template> savedTemplates = (List<Template>) templateRepository.saveAll(templates);
        log.info("{} templates successfully inserted", savedTemplates.size());
        return savedTemplates;
    }

    public void bulkDeleteTemplates(List<String> ids) {
        log.info("Bulk deleting {} templates", ids.size());
        ids.forEach(id -> {
            if (!templateRepository.existsById(id)) {
                log.warn("Template with ID {} not found; skipping deletion", id);
            }
        });
        templateRepository.deleteAllById(ids);
        log.info("{} templates successfully deleted", ids.size());
    }




}