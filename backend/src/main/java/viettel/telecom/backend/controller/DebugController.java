package viettel.telecom.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import viettel.telecom.backend.service.elasticsearch.ElasticsearchDatahubService;

@RestController
@RequestMapping("/api/debug")
public class DebugController {
    private final ElasticsearchDatahubService elasticsearchDatahubService;

    public DebugController(ElasticsearchDatahubService elasticsearchDatahubService) {
        this.elasticsearchDatahubService = elasticsearchDatahubService;
    }

    @GetMapping("/test-elasticsearch")
    public ResponseEntity<String> testElasticsearchConnection() {
        try {
            elasticsearchDatahubService.testConnection();
            return ResponseEntity.ok("Elasticsearch connection is successful.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to connect to Elasticsearch: " + e.getMessage());
        }
    }
}

