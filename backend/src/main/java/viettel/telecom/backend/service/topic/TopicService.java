package viettel.telecom.backend.service.topic;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import viettel.telecom.backend.entity.topic.Topic;
import viettel.telecom.backend.repository.topic.TopicRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing Topic entities.
 */
@Service
@RequiredArgsConstructor
public class TopicService {

    private final TopicRepository topicRepository;

    /**
     * Creates a new Topic.
     */
    public Topic createTopic(Topic topic) {
        topic.setCreatedAt(Instant.now());
        topic.setUpdatedAt(Instant.now());
        if (topic.getFlowIds() == null) {
            topic.setFlowIds(new ArrayList<>());
        }
        return topicRepository.save(topic);
    }

    /**
     * Retrieves a Topic by its ID.
     */
    public Optional<Topic> getTopicById(String id) {
        return topicRepository.findById(id);
    }

    /**
     * Updates an existing Topic.
     */
    public Topic updateTopic(Topic topic) {
        topic.setUpdatedAt(Instant.now());
        return topicRepository.save(topic);
    }

    /**
     * Deletes a Topic by its ID.
     */
    public void deleteTopic(String id) {
        topicRepository.deleteById(id);
    }

    /**
     * Associates a Flow ID with a Topic.
     */
    public Topic addFlowToTopic(String topicId, String flowId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found with id: " + topicId));
        List<String> flowIds = topic.getFlowIds();
        if (flowIds == null) {
            flowIds = new ArrayList<>();
        }
        if (!flowIds.contains(flowId)) {
            flowIds.add(flowId);
        }
        topic.setFlowIds(flowIds);
        topic.setUpdatedAt(Instant.now());
        return topicRepository.save(topic);
    }

    /**
     * Removes a Flow ID association from a Topic.
     */
    public Topic removeFlowFromTopic(String topicId, String flowId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found with id: " + topicId));
        List<String> flowIds = topic.getFlowIds();
        if (flowIds != null && flowIds.contains(flowId)) {
            flowIds.remove(flowId);
        }
        topic.setUpdatedAt(Instant.now());
        return topicRepository.save(topic);
    }

    public List<Topic> getAllTopics() {
        Iterable<Topic> iterable = topicRepository.findAll();
        List<Topic> topics = new ArrayList<>();
        iterable.forEach(topics::add);
        return topics;
    }


}
