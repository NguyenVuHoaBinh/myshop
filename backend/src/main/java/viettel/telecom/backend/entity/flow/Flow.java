package viettel.telecom.backend.entity.flow;

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
    @NotBlank(message = "Role cannot be blank")
    private String role;

    @Field(type = FieldType.Text)
    @NotBlank(message = "Purpose cannot be blank")
    private String purpose;

    @Field(type = FieldType.Nested)
    @NotEmpty(message = "Steps cannot be empty")
    private List<Step> steps;

    @Field(type = FieldType.Text)
    @NotBlank(message = "CreatedBy cannot be blank")
    private String createdBy;

    @Field(type = FieldType.Date)
    @NotBlank(message = "CreatedAt cannot be blank")
    private String createdAt;

    @Field(type = FieldType.Date)
    private String updatedAt;

    @Data
    public static class Step {

        @Field(type = FieldType.Keyword)
        @NotBlank(message = "Step ID cannot be blank")
        private String id;

        @Field(type = FieldType.Keyword)
        @NotBlank(message = "Template ID cannot be blank")
        private String templateId;

        @Field(type = FieldType.Keyword)
        @NotBlank(message = "Action type cannot be blank")
        private String actionType; // interaction, llm, logic, data

        @Field(type = FieldType.Text)
        private String condition; // Optional, only for logic steps

        @Field(type = FieldType.Text)
        private String prompt; // Optional, only for interaction steps

        @Field(type = FieldType.Keyword)
        private String nextStepId;

        @Field(type = FieldType.Keyword)
        private String fallbackStepId;

        @Field(type = FieldType.Object)
        private Map<String, Object> llmConfig; // Optional, for LLM-specific configuration

        @Field(type = FieldType.Integer)
        private int timeout; // Timeout in seconds for interaction steps

        @Field(type = FieldType.Text)
        private String validationRules; // Regex for input validation
    }
}
