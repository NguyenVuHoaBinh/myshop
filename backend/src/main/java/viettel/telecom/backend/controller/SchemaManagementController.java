package viettel.telecom.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import viettel.telecom.backend.service.elasticsearch.SchemaSyncService;
import viettel.telecom.backend.service.elasticsearch.ElasticsearchDatahubService;
import viettel.telecom.backend.service.logging.LogManagementService;

import java.util.List;

@RestController
@RequestMapping("/api/schema")
public class SchemaManagementController {

    private final ElasticsearchDatahubService elasticsearchDatahubService;

    public SchemaManagementController(ElasticsearchDatahubService elasticsearchDatahubService) {
        this.elasticsearchDatahubService = elasticsearchDatahubService;
    }

    /**
     * Fetches table names with pagination support.
     */
    @GetMapping("/tables")
    public ResponseEntity<List<String>> getTableNames(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String filter) {
        List<String> tableNames = elasticsearchDatahubService.fetchTableNames(page, size, filter);
        return ResponseEntity.ok(tableNames);
    }

    /**
     * Fetches fields for a specific table.
     */
    @GetMapping("/fields/{tableName}")
    public ResponseEntity<List<String>> getTableFields(@PathVariable String tableName) {
        List<String> fields = elasticsearchDatahubService.fetchFields(tableName);
        return ResponseEntity.ok(fields);
    }
}


