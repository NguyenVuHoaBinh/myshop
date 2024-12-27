package viettel.telecom.backend.repository.promptbuilder;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import viettel.telecom.backend.entity.promptbuilder.Template;

@Repository
public interface TemplateRepository extends ElasticsearchRepository<Template, String> {
}