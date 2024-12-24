package viettel.telecom.backend.service.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.GetRequest;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import viettel.telecom.backend.entity.promptbuilder.ObjectField;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ElasticsearchDatahubService {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchDatahubService.class);
    private static final String INDEX_NAME = "crm_objects";
    
    private final ElasticsearchClient elasticsearchClient;

    public ElasticsearchDatahubService(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    public void testConnection() {
        try {
            var response = elasticsearchClient.info();
            logger.info("Elasticsearch connection successful: {}", response);
        } catch (Exception e) {
            logger.error("Failed to connect to Elasticsearch: {}", e.getMessage());
        }
    }


    /**
     * Bulk indexes a list of tables into Elasticsearch with retry logic.
     *
     * @param tables The list of tables to index.
     */
    @Retryable(
            value = IOException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 1.5)
    )
    public void bulkIndexTables(List<ObjectField> tables) {
        try {
            BulkRequest.Builder bulkRequestBuilder = new BulkRequest.Builder();

            for (ObjectField table : tables) {
                bulkRequestBuilder.operations(op -> op
                        .index(idx -> idx
                                .index(INDEX_NAME)
                                .id(table.getObjectName())
                                .document(table)
                        ));
            }

            BulkResponse bulkResponse = elasticsearchClient.bulk(bulkRequestBuilder.build());
            if (bulkResponse.errors()) {
                logger.error("Bulk indexing encountered errors. Details: {}", bulkResponse.items());
                throw new RuntimeException("Bulk indexing failed with errors. Check logs for details.");
            }

            logger.info("Bulk indexing completed successfully. Indexed {} tables.", tables.size());

        } catch (IOException e) {
            logger.error("Failed to bulk index tables into Elasticsearch after retries.", e);
            throw new RuntimeException("Bulk indexing failed after retries.", e);
        }
    }

    /**
     * Updates fields for a specific table in Elasticsearch with retry logic.
     *
     * @param tableName The name of the table.
     * @param fields    The fields to update for the table.
     */
    @Retryable(
            value = IOException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 1.5)
    )
    public void updateTableFields(String tableName, List<String> fields) {
        try {
            ObjectField objectField = new ObjectField();
            objectField.setObjectName(tableName);
            objectField.setFields(fields);

            elasticsearchClient.index(IndexRequest.of(i -> i
                    .index(INDEX_NAME)
                    .id(tableName)
                    .document(objectField)
            ));

            logger.info("Updated fields for table: {}", tableName);

        } catch (IOException e) {
            logger.error("Failed to update fields for table: {} after retries.", tableName, e);
            throw new RuntimeException("Field update failed after retries.", e);
        }
    }

    /**
     * Fetches table names from Elasticsearch with pagination and optional filtering.
     *
     * @param page   The page number (0-based index).
     * @param size   The number of entries per page.
     * @param filter Optional filter string to match table names.
     * @return A list of table names.
     */
    public List<String> fetchTableNames(int page, int size, String filter) {
        try {
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index(INDEX_NAME)
                    .from(page * size)
                    .size(size)
                    .query(q -> filter == null ? q.matchAll(m -> m)
                            : q.match(t -> t.field("objectName").query(filter)))
            );

            SearchResponse<ObjectField> response = elasticsearchClient.search(searchRequest, ObjectField.class);

            return response.hits().hits().stream()
                    .map(hit -> hit.source().getObjectName())
                    .collect(Collectors.toList());

        } catch (IOException e) {
            logger.error("Failed to fetch table names from Elasticsearch.", e);
            throw new RuntimeException("Failed to fetch table names from Elasticsearch.", e);
        }
    }

    /**
     * Fetches fields for a specific table from Elasticsearch.
     *
     * @param tableName The name of the table.
     * @return A list of fields for the specified table.
     */
    public List<String> fetchFields(String tableName) {
        try {
            GetRequest getRequest = GetRequest.of(g -> g
                    .index(INDEX_NAME)
                    .id(tableName)
            );

            GetResponse<ObjectField> response = elasticsearchClient.get(getRequest, ObjectField.class);

            if (response.found()) {
                return response.source().getFields();
            } else {
                logger.warn("Table '{}' not found in Elasticsearch.", tableName);
                return List.of();
            }

        } catch (IOException e) {
            logger.error("Failed to fetch fields for table: {}", tableName, e);
            throw new RuntimeException("Failed to fetch fields for table: " + tableName, e);
        }
    }

    /**
     * Recovery logic for bulk indexing failures.
     *
     * @param e      The IOException encountered.
     * @param tables The list of tables that failed to index.
     */
    @Recover
    public void recoverFromBulkIndexFailure(IOException e, List<ObjectField> tables) {
        logger.error("Bulk indexing failed after retries. Failed tables: {}", tables.stream()
                .map(ObjectField::getObjectName)
                .collect(Collectors.joining(", ")));
    }

    /**
     * Recovery logic for field update failures.
     *
     * @param e        The IOException encountered.
     * @param tableName The name of the table.
     * @param fields    The fields that failed to update.
     */
    @Recover
    public void recoverFromFieldUpdateFailure(IOException e, String tableName, List<String> fields) {
        logger.error("Field update failed after retries for table '{}'. Fields: {}", tableName, fields, e);
    }
}
