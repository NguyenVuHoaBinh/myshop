package viettel.telecom.backend.service.datahub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import viettel.telecom.backend.entity.ingestion.IngestionResult;
import viettel.telecom.backend.service.elasticsearch.SchemaSyncService;

@Service
public class DataHubIngestionService {
    private static final Logger logger = LoggerFactory.getLogger(DataHubIngestionService.class);

    private final IngestionProcessService ingestionProcessService;
    private final SchemaSyncService schemaSyncService;

    public DataHubIngestionService(IngestionProcessService ingestionProcessService,
                                   SchemaSyncService schemaSyncService) {
        this.ingestionProcessService = ingestionProcessService;
        this.schemaSyncService = schemaSyncService;
    }

    /**
     * Runs the ingestion pipeline and synchronizes metadata to Elasticsearch.
     *
     * @param databaseName The name of the database being ingested.
     * @return The result of the ingestion process.
     */
    public IngestionResult runIngestionPipelineAndSync(String databaseName) {
        try {
            logger.info("Starting ingestion pipeline for database: {}", databaseName);

            // Run the ingestion process
            Process process = ingestionProcessService.startProcess();
            IngestionResult result = ingestionProcessService.processOutput(process);

            // If ingestion is successful, synchronize metadata
            if ("Ingestion pipeline completed successfully.".equals(result.getMessage())) {
                logger.info("Ingestion successful. Synchronizing metadata for database: {}", databaseName);
                schemaSyncService.syncTableNamesAndFields(databaseName);
                logger.info("Metadata synchronization completed for database: {}", databaseName);
            } else {
                logger.warn("Ingestion completed with warnings or failures. Skipping metadata synchronization.");
            }

            return result;
        } catch (Exception e) {
            logger.error("Error during ingestion and metadata synchronization for database '{}': {}", databaseName, e.getMessage(), e);
            throw new IllegalStateException("Ingestion pipeline and metadata synchronization failed", e);
        }
    }
}
