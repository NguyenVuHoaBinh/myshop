package viettel.telecom.backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import viettel.telecom.backend.entity.ingestion.DbParamsWrapper;
import viettel.telecom.backend.entity.ingestion.IngestionResult;
import viettel.telecom.backend.service.datahub.DataHubIngestionService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ingestion")
public class IngestionController {
    private static final Logger logger = LoggerFactory.getLogger(IngestionController.class);

    private final DataHubIngestionService dataHubIngestionService;

    public IngestionController(DataHubIngestionService dataHubIngestionService) {
        this.dataHubIngestionService = dataHubIngestionService;
    }

    @PostMapping("/start")
    public ResponseEntity<?> startIngestion(@RequestBody DbParamsWrapper dbParamsWrapper) {
        try {
            logger.info("Starting ingestion...");
            Map<String, String> dbParams = dbParamsWrapper.getDbParams();
            IngestionResult result = dataHubIngestionService.runIngestionPipeline();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Ingestion failed: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Ingestion pipeline failed");
        }
    }
}
