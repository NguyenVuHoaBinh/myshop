package viettel.telecom.backend.service.flow;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import viettel.telecom.backend.entity.flow.Flow;
import viettel.telecom.backend.entity.flow.FlowSummary;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FlowService {

    private final ElasticsearchClient elasticsearchClient;

    public FlowService(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    /**
     * Create or update a flow in Elasticsearch.
     *
     * @param flow The flow to save.
     * @return The ID of the saved flow.
     * @throws IOException If an error occurs while interacting with Elasticsearch.
     */
    public String createFlow(Flow flow) throws IOException {
        // Auto-generate ID if not provided
        if (flow.getId() == null || flow.getId().isEmpty()) {
            flow.setId(UUID.randomUUID().toString());
        }

        // Set createdAt and updatedAt timestamps
        LocalDateTime now = LocalDateTime.now();
        if (flow.getCreatedAt() == null || flow.getCreatedAt().isEmpty()) {
            flow.setCreatedAt(now.toString());
        }
        flow.setUpdatedAt(now.toString());

        IndexResponse response = elasticsearchClient.index(i -> i
                .index("flows")
                .id(flow.getId())
                .document(flow)
        );

        log.info("Flow created/updated with ID: {}, createdAt: {}, updatedAt: {}",
                response.id(), flow.getCreatedAt(), flow.getUpdatedAt());
        return response.id();
    }

    /**
     * Retrieve a flow by its ID (full details).
     *
     * @param id The ID of the flow to retrieve.
     * @return The retrieved flow.
     * @throws IOException If an error occurs while interacting with Elasticsearch.
     */
    public Flow getFlow(String id) throws IOException {
        GetResponse<Flow> response = elasticsearchClient.get(g -> g
                .index("flows")
                .id(id), Flow.class);

        if (response.found()) {
            log.info("Flow retrieved: {}", id);
            return response.source();
        } else {
            log.error("Flow not found: {}", id);
            throw new IllegalArgumentException("Flow with ID " + id + " not found");
        }
    }

    /**
     * Retrieve a paginated list of flows (full details).
     *
     * @param page The page number (0-based).
     * @param size The number of items per page.
     * @return A list of flows for the specified page.
     * @throws IOException If an error occurs while interacting with Elasticsearch.
     */
    public List<Flow> listFlows(int page, int size) throws IOException {
        SearchResponse<Flow> response = elasticsearchClient.search(s -> s
                .index("flows")
                .from(page * size)
                .size(size), Flow.class);

        return response.hits().hits().stream()
                .map(Hit::source)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve a paginated list of flow summaries (id, name, description).
     *
     * @param page The page number (0-based).
     * @param size The number of items per page.
     * @return A list of flow summaries for the specified page.
     * @throws IOException If an error occurs while interacting with Elasticsearch.
     */
    public List<FlowSummary> listFlowSummaries(int page, int size) throws IOException {
        // We request only certain fields to reduce payload,
        // but note that partial hits may map fine if the missing fields are ignored.
        // If you want to strictly retrieve only these fields from ES,
        // you can specify a source filter. For example:
        //
        //    .source(config -> config.filter(f -> f
        //            .includes("id", "name", "description")))
        //
        // Below is a simple approach that retrieves everything, then we map to FlowSummary.

        SearchResponse<Flow> response = elasticsearchClient.search(s -> s
                .index("flows")
                .from(page * size)
                .size(size), Flow.class);

        return response.hits().hits().stream()
                .map(Hit::source)
                .map(flow -> new FlowSummary(
                        flow.getId(),
                        flow.getName(),
                        flow.getDescription()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Delete a flow by its ID.
     *
     * @param flowId The ID of the flow to delete.
     * @throws IOException If an error occurs while interacting with Elasticsearch.
     */
    public void deleteFlowById(String flowId) throws IOException {
        DeleteResponse response = elasticsearchClient.delete(d -> d
                .index("flows")
                .id(flowId)
        );

        if ("not_found".equalsIgnoreCase(response.result().jsonValue())) {
            log.error("Flow with ID {} does not exist", flowId);
            throw new IllegalArgumentException("Flow with ID " + flowId + " does not exist");
        }

        log.info("Flow deleted successfully with ID: {}", flowId);
    }

    /**
     * Update a flow partially.
     *
     * @param flowId  The ID of the flow to update.
     * @param updates A map of fields to update.
     * @throws IOException If an error occurs while interacting with Elasticsearch.
     */
    public void updateFlow(String flowId, Map<String, Object> updates) throws IOException {
        // Add 'updatedAt' to track partial update time
        updates.put("updatedAt", LocalDateTime.now().toString());

        UpdateResponse<Flow> response = elasticsearchClient.update(u -> u
                .index("flows")
                .id(flowId)
                .doc(updates), Flow.class);

        log.info("Flow with ID {} updated successfully", response.id());
    }
}
