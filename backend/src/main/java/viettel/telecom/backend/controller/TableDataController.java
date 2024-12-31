package viettel.telecom.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import viettel.telecom.backend.service.elasticsearch.ElasticsearchDatahubService;
import viettel.telecom.backend.entity.promptbuilder.ObjectField;

import java.util.List;

@RestController
@RequestMapping("/api/tables")
public class TableDataController {

    private final ElasticsearchDatahubService elasticsearchDatahubService;

    @Autowired
    public TableDataController(ElasticsearchDatahubService elasticsearchDatahubService) {
        this.elasticsearchDatahubService = elasticsearchDatahubService;
    }

    /**
     * Fetches table names stored in Elasticsearch with optional pagination and filtering.
     *
     * @param page   The page number (default is 0).
     * @param size   The number of items per page (default is 10).
     * @param filter Optional filter string to match table names.
     * @return A list of table names.
     */
    @GetMapping
    public ResponseEntity<List<ObjectField>> getTableNames(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String filter) {
        try {
            List<ObjectField> tableNames = elasticsearchDatahubService.fetchTableNamesWithFields(page, size, filter);
            return ResponseEntity.ok(tableNames);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Fetches fields for a specific table by its name.
     *
     * @param tableName The name of the table.
     * @return A list of fields for the table.
     */
    @GetMapping("/{tableName}/fields")
    public ResponseEntity<List<String>> getTableFields(@PathVariable String tableName) {
        try {
            List<String> fields = elasticsearchDatahubService.fetchFields(tableName);
            return ResponseEntity.ok(fields);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
}
