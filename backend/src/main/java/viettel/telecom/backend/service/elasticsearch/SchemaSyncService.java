package viettel.telecom.backend.service.elasticsearch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import viettel.telecom.backend.entity.promptbuilder.ObjectField;
import viettel.telecom.backend.service.datahub.GraphQLService;

import java.util.ArrayList;
import java.util.List;

@Service
public class SchemaSyncService {

    private static final Logger logger = LoggerFactory.getLogger(SchemaSyncService.class);

    private final GraphQLService graphQLService;
    private final ElasticsearchDatahubService elasticsearchDatahubService;
    private final ObjectMapper objectMapper;

    public SchemaSyncService(GraphQLService graphQLService,
                             ElasticsearchDatahubService elasticsearchDatahubService,
                             ObjectMapper objectMapper) {
        this.graphQLService = graphQLService;
        this.elasticsearchDatahubService = elasticsearchDatahubService;
        this.objectMapper = objectMapper;
    }

    /**
     * Synchronizes table names for a given database by fetching schema details
     * from DataHub and indexing them into Elasticsearch.
     *
     * @param databaseName The name of the database.
     */
    public void syncTableNames(String databaseName) {
        logger.info("Starting synchronization of table names for database: {}", databaseName);

        try {
            // Fetch table names from DataHub via GraphQL
            String tableNamesJson = graphQLService.fetchTableNames(databaseName);
            JsonNode tableNamesNode = objectMapper.readTree(tableNamesJson);

            // Build ObjectField objects for each table
            List<ObjectField> tables = new ArrayList<>();
            for (JsonNode tableNode : tableNamesNode) {
                ObjectField objectField = new ObjectField();
                objectField.setObjectName(tableNode.asText());
                objectField.setDatabaseName(databaseName);
                tables.add(objectField);
            }

            logger.info("Fetched {} table names for database: {}", tables.size(), databaseName);

            // Bulk index table names into Elasticsearch
            elasticsearchDatahubService.bulkIndexTables(tables);
            logger.info("Successfully synchronized table names for database: {}", databaseName);

        } catch (Exception e) {
            logger.error("Error syncing table names for database '{}': {}", databaseName, e.getMessage(), e);
            throw new RuntimeException("Failed to sync table names for database: " + databaseName, e);
        }
    }

    /**
     * Synchronizes fields for a specific table by fetching metadata
     * from DataHub and updating Elasticsearch.
     *
     * @param tableName The name of the table.
     */
    public void syncTableFields(String tableName) {
        logger.info("Starting synchronization of fields for table: {}", tableName);

        try {
            // Fetch field metadata from DataHub via GraphQL
            String fieldsJson = graphQLService.fetchTableFields(tableName);
            JsonNode fieldsNode = objectMapper.readTree(fieldsJson);

            // Extract field details
            List<String> fields = new ArrayList<>();
            for (JsonNode fieldNode : fieldsNode) {
                fields.add(fieldNode.path("Field").asText());
            }

            logger.info("Fetched {} fields for table: {}", fields.size(), tableName);

            // Update fields for the specified table in Elasticsearch
            elasticsearchDatahubService.updateTableFields(tableName, fields);
            logger.info("Successfully synchronized fields for table: {}", tableName);

        } catch (Exception e) {
            logger.error("Error syncing fields for table '{}': {}", tableName, e.getMessage(), e);
            throw new RuntimeException("Failed to sync fields for table: " + tableName, e);
        }
    }
}
