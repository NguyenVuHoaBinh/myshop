package viettel.telecom.backend.service.flow;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import viettel.telecom.backend.entity.flow.Flow;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FlowService {

    private final ElasticsearchClient elasticsearchClient;
    private final FlowExecutor flowExecutor;

    public FlowService(ElasticsearchClient elasticsearchClient, FlowExecutor flowExecutor) {
        this.elasticsearchClient = elasticsearchClient;
        this.flowExecutor = flowExecutor;
    }

    public String createFlow(Flow flow) throws IOException {
        flow.setId(java.util.UUID.randomUUID().toString());
        IndexResponse response = elasticsearchClient.index(i -> i
                .index("flows")
                .id(flow.getId())
                .document(flow)
        );
        log.info("Flow created with ID: {}", response.id());
        return response.id();
    }

    public Flow getFlow(String id) throws IOException {
        var response = elasticsearchClient.get(g -> g
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

    public List<Flow> searchFlows(String role, String purpose) throws IOException {
        SearchResponse<Flow> response = elasticsearchClient.search(s -> s
                .index("flows")
                .query(q -> q.bool(b -> b
                        .must(m -> m.term(t -> t.field("role").value(role)))
                        .must(m -> m.match(mt -> mt.field("purpose").query(purpose)))
                )), Flow.class);

        return response.hits().hits().stream()
                .map(hit -> hit.source())
                .collect(Collectors.toList());
    }

    public void deleteFlowById(String flowId) throws IOException {
        var response = elasticsearchClient.delete(d -> d
                .index("flows")
                .id(flowId)
        );

        if (response.result().name().equalsIgnoreCase("not_found")) {
            throw new IllegalArgumentException("Flow with ID " + flowId + " does not exist");
        }

        log.info("Flow deleted successfully with ID: {}", flowId);
    }

    public void executeFlowById(String flowId, Map<String, Object> initialContext, WebSocketSession session) throws IOException {
        Flow flow = getFlow(flowId);
        flowExecutor.executeFlow(flow, initialContext, session);
    }

    public List<Flow> getAllFlows() throws IOException {
        SearchResponse<Flow> response = elasticsearchClient.search(s -> s
                .index("flows")
                .query(q -> q.matchAll(m -> m)), Flow.class);

        return response.hits().hits().stream()
                .map(hit -> hit.source())
                .collect(Collectors.toList());
    }

}
