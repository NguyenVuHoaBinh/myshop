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
     * Synchronizes table names and fields for a given database.
     *
     * @param databaseName The name of the database.
     */
    public void syncTableNamesAndFields(String databaseName) {
        logger.info("Starting synchronization of table names and fields for database: {}", databaseName);

        try {
            // Fetch table names from DataHub via GraphQL
            String tableNamesJson = graphQLService.fetchTableNames(databaseName);
            JsonNode tableNamesNode = objectMapper.readTree(tableNamesJson);

            // Build ObjectField objects for each table, including fields
            List<ObjectField> tables = new ArrayList<>();
            for (JsonNode tableNode : tableNamesNode) {
                String tableName = tableNode.asText();

                // Fetch fields for each table
                String fieldsJson = graphQLService.fetchTableFields(tableName);
                JsonNode fieldsNode = objectMapper.readTree(fieldsJson);

                List<String> fields = new ArrayList<>();
                for (JsonNode fieldNode : fieldsNode) {
                    fields.add(fieldNode.path("Field").asText());
                }

                ObjectField objectField = new ObjectField();
                objectField.setObjectName(tableName);
                objectField.setFields(fields);
                objectField.setDatabaseName(databaseName);
                tables.add(objectField);
            }

            logger.info("Fetched {} tables with fields for database: {}", tables.size(), databaseName);

            // Bulk index table names and fields into Elasticsearch
            elasticsearchDatahubService.bulkIndexTablesWithFields(tables);
            logger.info("Successfully synchronized table names and fields for database: {}", databaseName);

        } catch (Exception e) {
            logger.error("Error syncing table names and fields for database '{}': {}", databaseName, e.getMessage(), e);
            throw new RuntimeException("Failed to sync table names and fields for database: " + databaseName, e);
        }
    }
}
