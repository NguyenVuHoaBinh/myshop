package viettel.telecom.backend.repository.flowbuilder;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import viettel.telecom.backend.entity.flow.Flow;

@Repository
public interface FlowRepository extends ElasticsearchRepository<Flow, String> {
    // If using JPA: extends JpaRepository<Flow, String>
}
