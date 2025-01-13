package viettel.telecom.backend.service.flow;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import viettel.telecom.backend.entity.flow.Flow;

import java.io.IOException;
import java.util.List;
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
        // Generate a new ID if the flow does not have one
        if (flow.getId() == null || flow.getId().isEmpty()) {
            flow.setId(UUID.randomUUID().toString());
        }

        IndexResponse response = elasticsearchClient.index(i -> i
                .index("flows")
                .id(flow.getId())
                .document(flow)
        );

        log.info("Flow created/updated with ID: {}", response.id());
        return response.id();
    }

    /**
     * Retrieve a flow by its ID.
     *
     * @param id The ID of the flow to retrieve.
     * @return The retrieved flow.
     * @throws IOException If an error occurs while interacting with Elasticsearch.
     */
    public Flow getFlow(String id) throws IOException {
        GetResponse<Flow> response = elasticsearchClient.get(g -> g
                .index("flows")
                .id(id), Flow.class
        );

        if (response.found()) {
            log.info("Flow retrieved: {}", id);
            return response.source();
        } else {
            log.error("Flow not found: {}", id);
            throw new IllegalArgumentException("Flow with ID " + id + " not found");
        }
    }

    /**
     * Search for flows based on role and purpose.
     *
     * @param role    The role to filter by.
     * @param purpose The purpose to filter by.
     * @return A list of matching flows.
     * @throws IOException If an error occurs while interacting with Elasticsearch.
     */
    public List<Flow> searchFlows(String role, String purpose) throws IOException {
        SearchResponse<Flow> response = elasticsearchClient.search(s -> s
                .index("flows")
                .query(q -> q.bool(b -> b
                        .must(m -> m.term(t -> t.field("role.keyword").value(role)))
                        .must(m -> m.match(mt -> mt.field("purpose").query(purpose)))
                )), Flow.class);

        return response.hits().hits().stream()
                .map(Hit::source)
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
     * List all flows with optional pagination.
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
}
