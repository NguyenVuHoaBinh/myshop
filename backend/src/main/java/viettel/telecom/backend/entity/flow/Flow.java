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

    @Data
    public static class Node {

        @Field(type = FieldType.Keyword)
        @NotBlank(message = "Node ID cannot be blank")
        private String id;

        @Field(type = FieldType.Keyword)
        @NotBlank(message = "Node type cannot be blank")
        private String type; // e.g., startNode, endNode, interactionNode, llmNode, logicNode

        @Field(type = FieldType.Object)
        private Position position;

        @Field(type = FieldType.Object)
        private NodeData data;

        @Data
        public static class Position {

            @Field(type = FieldType.Float)
            private float x;

            @Field(type = FieldType.Float)
            private float y;
        }

        @Data
        public static class NodeData {

            @Field(type = FieldType.Text)
            private String label;

            @Field(type = FieldType.Text)
            private String name;

            @Field(type = FieldType.Text)
            private String botResponse;

            @Field(type = FieldType.Keyword)
            private String templateId;

            @Field(type = FieldType.Object)
            private SelectedTemplate selectedTemplate;

            @Data
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

                @Field(type = FieldType.Keyword)
                private String aiModel;

                // Add these two fields to match your JSON payload
                @Field(type = FieldType.Date)
                private String createdAt;

                @Field(type = FieldType.Date)
                private String updatedAt;

                @Data
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
        private String type; // e.g., animatedEdge
    }
}
