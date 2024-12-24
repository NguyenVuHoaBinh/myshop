package viettel.telecom.backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import viettel.telecom.backend.entity.ingestion.DbParamsWrapper;
import viettel.telecom.backend.entity.ingestion.IngestionResult;
import viettel.telecom.backend.service.datahub.DataHubIngestionService;
import viettel.telecom.backend.service.elasticsearch.ElasticsearchDatahubService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ingestion")
public class IngestionController {

    private static final Logger logger = LoggerFactory.getLogger(IngestionController.class);

    private final DataHubIngestionService dataHubIngestionService;
    private final ElasticsearchDatahubService elasticsearchDatahubService;

    public IngestionController(DataHubIngestionService dataHubIngestionService,
                               ElasticsearchDatahubService elasticsearchDatahubService) {
        this.dataHubIngestionService = dataHubIngestionService;
        this.elasticsearchDatahubService = elasticsearchDatahubService;
    }

    /**
     * Step 1: Validate user parameters for database connection.
     */
    @PostMapping("/validate-params")
    public ResponseEntity<?> validateParams(@RequestBody DbParamsWrapper dbParamsWrapper) {
        try {
            Map<String, String> dbParams = dbParamsWrapper.getDbParams();

            if (dbParams.get("database") == null || dbParams.get("database").isBlank()) {
                return ResponseEntity.badRequest().body("Database name is required.");
            }

            logger.info("Parameters validated successfully.");
            return ResponseEntity.ok("Parameters are valid. Ready to connect.");
        } catch (Exception e) {
            logger.error("Error validating parameters: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error validating parameters: " + e.getMessage());
        }
    }

    /**
     * Step 2: Start the ingestion process and synchronize metadata.
     */
    @PostMapping("/start")
    public ResponseEntity<?> startIngestion(@RequestBody DbParamsWrapper dbParamsWrapper) {
        try {
            String databaseName = dbParamsWrapper.getDbParams().get("database");
            if (databaseName == null || databaseName.isBlank()) {
                return ResponseEntity.badRequest().body("Database name is required.");
            }

            logger.info("Starting ingestion pipeline for database: {}", databaseName);
            IngestionResult result = dataHubIngestionService.runIngestionPipelineAndSync(databaseName);

            logger.info("Ingestion and metadata synchronization completed.");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Ingestion failed: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Ingestion pipeline failed: " + e.getMessage());
        }
    }

    /**
     * Step 3a: Fetch table names from Elasticsearch for metadata consumption.
     */
    @GetMapping("/tables")
    public ResponseEntity<?> fetchTableNames(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        try {
            List<String> tableNames = elasticsearchDatahubService.fetchTableNamesWithFields(page, size, null)
                    .stream()
                    .map(table -> table.getObjectName())
                    .toList();

            return ResponseEntity.ok(tableNames);
        } catch (Exception e) {
            logger.error("Error fetching table names: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to fetch table names: " + e.getMessage());
        }
    }

    /**
     * Step 3b: Fetch fields for a specific table from Elasticsearch.
     */
    @GetMapping("/tables/{tableName}/fields")
    public ResponseEntity<?> fetchTableFields(@PathVariable String tableName) {
        try {
            List<String> fields = elasticsearchDatahubService.fetchFields(tableName);
            return ResponseEntity.ok(fields);
        } catch (Exception e) {
            logger.error("Error fetching fields for table '{}': {}", tableName, e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to fetch fields for table '" + tableName + "': " + e.getMessage());
        }
    }
}
