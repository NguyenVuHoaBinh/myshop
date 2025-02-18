package viettel.telecom.backend.repository.topic;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import viettel.telecom.backend.entity.topic.Topic;

/**
 * Repository interface for Topic entities.
 */
public interface TopicRepository extends ElasticsearchRepository<Topic, String> {
}