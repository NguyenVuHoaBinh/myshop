package viettel.telecom.backend.entity.flow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;

@Data
@Document(indexName = "flows") // Elasticsearch index for flows
@JsonIgnoreProperties(ignoreUnknown = true) // Ignore any extra fields at the top level
public class Flow {

    @Id
    @NotBlank(message = "Flow ID cannot be blank")
    private String id;

    @Field(type = FieldType.Text)
    @NotBlank(message = "Flow name cannot be blank")
    private String name;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Text)
    @NotBlank(message = "Role cannot be blank")
    private String role;

    @Field(type = FieldType.Text)
    @NotBlank(message = "Purpose cannot be blank")
    private String purpose;

    @Field(type = FieldType.Date)
    @NotBlank(message = "CreatedAt cannot be blank")
    private String createdAt;

    @Field(type = FieldType.Date)
    private String updatedAt;

    @Field(type = FieldType.Nested)
    @NotEmpty(message = "Nodes cannot be empty")
    private List<Node> nodes;

    @Field(type = FieldType.Nested)
    private List<Edge> edges;

    @Field(type = FieldType.Text)
    @NotBlank(message = "CreatedBy cannot be blank")
    private String createdBy;

    // -------------------------------------------------------------
    // Inner classes: Node, Edge
    // -------------------------------------------------------------
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Node {

        @Field(type = FieldType.Keyword)
        @NotBlank(message = "Node ID cannot be blank")
        private String id;

        @Field(type = FieldType.Keyword)
        @NotBlank(message = "Node type cannot be blank")
        private String type; // e.g., "DATA", "LLM", "LOGIC", etc.

        @Field(type = FieldType.Object)
        private Position position;

        @Field(type = FieldType.Object)
        private NodeData data;

        // If your flow engine uses a 'next' property at the node level,
        // keep it here or manage it via edges. Some flows store next node as edges only.
        // We'll keep it optional in case you want it.
        @Field(type = FieldType.Keyword)
        private String next;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Position {
            @Field(type = FieldType.Float)
            private float x;

            @Field(type = FieldType.Float)
            private float y;
        }

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class NodeData {

            @Field(type = FieldType.Text)
            private String label;

            @Field(type = FieldType.Text)
            private String name;

            @Field(type = FieldType.Text)
            private String botResponse;

            @Field(type = FieldType.Keyword)
            private String templateId;

            // This field is for selected template details (LLM usage, etc.)
            @Field(type = FieldType.Object)
            private SelectedTemplate selectedTemplate;

            // Indicates whether the conversation should be shown (defaults to true)
            @Field(type = FieldType.Boolean)
            private Boolean showConversation;

            // LLM config group
            @Field(type = FieldType.Object)
            private LLMConfig llmconfig;

            // -----------------------------
            // NEW FIELDS FOR DATA NODE
            // -----------------------------

            /**
             * The URL endpoint to which we'll POST data.
             * May contain placeholders like "/api/[entity]/create".
             */
            @Field(type = FieldType.Text)
            private String requestUrl;

            /**
             * The body payload for the POST request.
             * This can be any JSON structure, so we store it as a Map.
             * It may contain placeholders like "[groupName]".
             */
            @Field(type = FieldType.Object)
            private Map<String, Object> requestBody;

            /**
             * The next node ID if this request is successful.
             */
            @Field(type = FieldType.Text)
            private String onSuccessNextNode;

            /**
             * The next node ID if this request fails (error fallback).
             */
            @Field(type = FieldType.Text)
            private String onErrorNextNode;

            // ---------------------------------------------------------
            // Inner classes for LLM usage or selected templates
            // ---------------------------------------------------------

            @Data
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class LLMConfig {
                @Field(type = FieldType.Keyword)
                private String aiModel; // e.g., "gpt-4o", "gpt-4o-mini", etc.

                @Field(type = FieldType.Float)
                private Double temperature;

                @Field(type = FieldType.Integer)
                private Integer max_tokens;

                @Field(type = FieldType.Boolean)
                private Boolean stream;
            }

            @Data
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class SelectedTemplate {

                @Field(type = FieldType.Keyword)
                private String id;

                @Field(type = FieldType.Text)
                private String name;

                @Field(type = FieldType.Text)
                private String description;

                @Field(type = FieldType.Text)
                private String type;

                @Field(type = FieldType.Text)
                private String object;

                @Field(type = FieldType.Text)
                private String objectField;

                @Field(type = FieldType.Nested)
                private List<FieldDetails> fields;

                @Field(type = FieldType.Text)
                private String systemPrompt;

                @Field(type = FieldType.Date)
                private String createdAt;

                @Field(type = FieldType.Date)
                private String updatedAt;

                @Data
                @JsonIgnoreProperties(ignoreUnknown = true)
                public static class FieldDetails {
                    @Field(type = FieldType.Text)
                    private String fieldName;

                    @Field(type = FieldType.Keyword)
                    private String fieldType;
                }
            }
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Edge {

        @Field(type = FieldType.Keyword)
        @NotBlank(message = "Edge ID cannot be blank")
        private String id;

        @Field(type = FieldType.Keyword)
        @NotBlank(message = "Source node ID cannot be blank")
        private String source;

        @Field(type = FieldType.Keyword)
        @NotBlank(message = "Target node ID cannot be blank")
        private String target;

        @Field(type = FieldType.Keyword)
        @NotBlank(message = "Edge type cannot be blank")
        private String type; // e.g., "animatedEdge"
    }
}
