package viettel.telecom.backend.config.nosql.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ElasticsearchIndexConfig {

    private final ElasticsearchClient elasticsearchClient;

    public ElasticsearchIndexConfig(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    public void createFlowIndex() throws IOException {
        CreateIndexRequest request = new CreateIndexRequest.Builder()
                .index("flows")
                .mappings(m -> m
                        .properties("id", p -> p.keyword(k -> k))
                        .properties("name", p -> p.text(t -> t))
                        .properties("role", p -> p.text(t -> t))
                        .properties("purpose", p -> p.text(t -> t))
                        .properties("steps", p -> p.nested(n -> n
                                .properties("id", sp -> sp.keyword(k -> k))
                                .properties("templateId", sp -> sp.keyword(k -> k))
                                .properties("actionType", sp -> sp.keyword(k -> k))
                                .properties("condition", sp -> sp.text(t -> t))
                                .properties("prompt", sp -> sp.text(t -> t))
                                .properties("nextStepId", sp -> sp.keyword(k -> k))
                                .properties("fallbackStepId", sp -> sp.keyword(k -> k))
                                .properties("llmConfig", sp -> sp.object(o -> o))
                                .properties("timeout", sp -> sp.integer(i -> i))
                                .properties("validationRules", sp -> sp.text(t -> t))
                        ))
                        .properties("createdBy", p -> p.text(t -> t))
                        .properties("createdAt", p -> p.date(d -> d))
                        .properties("updatedAt", p -> p.date(d -> d))
                )
                .build();

        CreateIndexResponse response = elasticsearchClient.indices().create(request);
        if (response.acknowledged()) {
            System.out.println("Index 'flows' created successfully.");
        } else {
            System.err.println("Failed to create index 'flows'.");
        }
    }
}