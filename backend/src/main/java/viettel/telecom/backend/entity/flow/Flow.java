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

/**
 * Represents a flow structure stored in Elasticsearch index "flows".
 *
 * For logic nodes, we now support two ways:
 *   1) Single expression (botResponse/templateId/label).
 *   2) Multi-branch logic with List<LogicCase>.
 */
@Data
@Document(indexName = "flows")
@JsonIgnoreProperties(ignoreUnknown = true)
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

        // Optional, if you want a fallback "next" at the node level:
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
            private String label;       // Possibly used as fallback or display text

            @Field(type = FieldType.Text)
            private String name;

            /**
             * SINGLE EXPRESSION:
             *   - For a "logic" node,
             *   - e.g. "userInput == 'Yes'"
             */
            @Field(type = FieldType.Text)
            private String botResponse;

            /**
             * SINGLE EXPRESSION:
             *   - If condition is true => go to templateId
             */
            @Field(type = FieldType.Keyword)
            private String templateId;

            /**
             * MULTI-BRANCH LOGIC:
             *   - A list of (expression => nextNode) pairs
             */
            @Field(type = FieldType.Nested)
            private List<LogicCase> logicCases;

            // Additional fields for LLM, Data Node, etc.
            @Field(type = FieldType.Boolean)
            private Boolean showConversation;

            @Field(type = FieldType.Object)
            private LLMConfig llmconfig;

            @Field(type = FieldType.Text)
            private String requestUrl;

            @Field(type = FieldType.Object)
            private Map<String, Object> requestBody;

            @Field(type = FieldType.Text)
            private String onSuccessNextNode;

            @Field(type = FieldType.Text)
            private String onErrorNextNode;

            @Field(type = FieldType.Object)
            private SelectedTemplate selectedTemplate;

            @Data
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class LLMConfig {
                @Field(type = FieldType.Keyword)
                private String aiModel;
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
