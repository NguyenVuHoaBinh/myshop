package viettel.telecom.backend.service.datahub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import viettel.telecom.backend.entity.ingestion.IngestionResult;

@Service
public class DataHubIngestionService {
    private static final Logger logger = LoggerFactory.getLogger(DataHubIngestionService.class);
    private final IngestionProcessService ingestionProcessService;

    public DataHubIngestionService(IngestionProcessService ingestionProcessService) {
        this.ingestionProcessService = ingestionProcessService;
    }

    public IngestionResult runIngestionPipeline() {
        try {
            logger.info("Starting ingestion pipeline...");
            Process process = ingestionProcessService.startProcess();
            IngestionResult result = ingestionProcessService.processOutput(process);
            logger.info("Ingestion pipeline completed successfully.");
            return result;
        } catch (Exception e) {
            logger.error("Error during ingestion pipeline execution: {}", e.getMessage(), e);
            throw new IllegalStateException("Ingestion pipeline failed", e);
        }
    }
}
