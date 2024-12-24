package viettel.telecom.backend.service.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
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
import java.util.ArrayList;
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

    /**
     * Tests the connection to Elasticsearch.
     */
    public void testConnection() {
        try {
            var response = elasticsearchClient.info();
            logger.info("Elasticsearch connection successful: {}", response);
        } catch (Exception e) {
            logger.error("Failed to connect to Elasticsearch: {}", e.getMessage());
        }
    }

    /**
     * Bulk indexes a list of tables with fields into Elasticsearch.
     *
     * @param tables The list of tables to index with their fields.
     */
    @Retryable(
            value = IOException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 1.5)
    )
    public void bulkIndexTablesWithFields(List<ObjectField> tables) {
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
                throw new RuntimeException("Bulk indexing with fields failed.");
            }

            logger.info("Successfully indexed {} tables with fields.", tables.size());
        } catch (IOException e) {
            logger.error("Failed to bulk index tables with fields into Elasticsearch.", e);
            throw new RuntimeException("Bulk indexing with fields failed.", e);
        }
    }

    /**
     * Recovery logic for bulk indexing failures.
     *
     * @param e      The IOException encountered.
     * @param tables The list of tables that failed to index.
     */
    @Recover
    public void recoverBulkIndexFailure(IOException e, List<ObjectField> tables) {
        logger.error("Bulk indexing with fields failed after retries. Failed tables: {}", tables.stream()
                .map(ObjectField::getObjectName)
                .collect(Collectors.joining(", ")), e);
    }

    /**
     * Fetches table names along with their fields from Elasticsearch.
     *
     * @param page   The page number (0-based index).
     * @param size   The number of entries per page.
     * @param filter Optional filter string to match table names.
     * @return A list of ObjectField containing table names and fields.
     */
    @Retryable(
            value = IOException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 1.5)
    )
    public List<ObjectField> fetchTableNamesWithFields(int page, int size, String filter) {
        try {
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index(INDEX_NAME)
                    .from(page * size)
                    .size(size)
                    .query(q -> filter == null ? q.matchAll(m -> m)
                            : q.match(t -> t.field("objectName").query(filter)))
            );

            SearchResponse<ObjectField> response = elasticsearchClient.search(searchRequest, ObjectField.class);

            List<ObjectField> tables = new ArrayList<>();
            for (var hit : response.hits().hits()) {
                if (hit.source() != null) {
                    tables.add(hit.source());
                }
            }
            return tables;

        } catch (IOException e) {
            logger.error("Failed to fetch table names with fields from Elasticsearch.", e);
            throw new RuntimeException("Failed to fetch table names with fields.", e);
        }
    }

    /**
     * Recovery logic for fetch operations.
     *
     * @param e      The IOException encountered.
     * @param page   The page number.
     * @param size   The size of the page.
     * @param filter The filter applied.
     * @return An empty list as fallback.
     */
    @Recover
    public List<ObjectField> recoverFetchTableNamesFailure(IOException e, int page, int size, String filter) {
        logger.error("Fetching table names with fields failed after retries. Page: {}, Size: {}, Filter: {}",
                page, size, filter, e);
        return List.of(); // Return an empty list as a fallback
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
}
