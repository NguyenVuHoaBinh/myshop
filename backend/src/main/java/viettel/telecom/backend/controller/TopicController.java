package viettel.telecom.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import viettel.telecom.backend.entity.topic.Topic;
import viettel.telecom.backend.service.topic.TopicService;

import java.util.Optional;

/**
 * REST Controller for managing Topic entities.
 */
@RestController
@RequestMapping("/api/topics")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;

    @PostMapping
    public ResponseEntity<Topic> createTopic(@RequestBody Topic topic) {
        Topic createdTopic = topicService.createTopic(topic);
        return new ResponseEntity<>(createdTopic, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Topic> getTopic(@PathVariable String id) {
        Optional<Topic> topicOptional = topicService.getTopicById(id);
        return topicOptional.map(topic -> new ResponseEntity<>(topic, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Topic> updateTopic(@PathVariable String id, @RequestBody Topic topic) {
        // Ensure the Topic ID matches the path variable.
        topic.setId(id);
        Topic updatedTopic = topicService.updateTopic(topic);
        return new ResponseEntity<>(updatedTopic, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTopic(@PathVariable String id) {
        topicService.deleteTopic(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{id}/flows/{flowId}")
    public ResponseEntity<Topic> addFlowToTopic(@PathVariable String id, @PathVariable String flowId) {
        Topic updatedTopic = topicService.addFlowToTopic(id, flowId);
        return new ResponseEntity<>(updatedTopic, HttpStatus.OK);
    }

    @DeleteMapping("/{id}/flows/{flowId}")
    public ResponseEntity<Topic> removeFlowFromTopic(@PathVariable String id, @PathVariable String flowId) {
        Topic updatedTopic = topicService.removeFlowFromTopic(id, flowId);
        return new ResponseEntity<>(updatedTopic, HttpStatus.OK);
    }
}