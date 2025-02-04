package viettel.telecom.backend.entity.promptbuilder;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

@Data
@Document(indexName = "templates")
public class Template {

    @Id
    @NotBlank(message = "ID cannot be blank")
    private String id;

    @Field(type = FieldType.Text)
    @NotBlank(message = "Name cannot be blank")
    @Size(max = 255, message = "Name cannot exceed 255 characters")
    private String name;

    @Field(type = FieldType.Text)
    @NotBlank(message = "Object name cannot be blank")
    private String object;

    @Field(type = FieldType.Text)
    @NotBlank(message = "Object field cannot be blank")
    private String objectField;

    @Field(type = FieldType.Text)
    @NotBlank(message = "Description cannot be blank")
    private String description;

    @Field(type = FieldType.Keyword)
    @NotBlank(message = "Type cannot be blank")
    private String type;

    @Field(type = FieldType.Nested)
    private List<FieldMapping> fields;

    @Field(type = FieldType.Text)
    private String systemPrompt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Field(type = FieldType.Date)
    private LocalDate createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Field(type = FieldType.Date)
    private LocalDate updatedAt;

    // Grouping LLM configuration settings into a single object field.
    @Field(type = FieldType.Object)
    @NotNull(message = "LLM configuration cannot be null")
    private LLMConfig llmconfig;

    @Data
    public static class FieldMapping {
        @Field(type = FieldType.Keyword)
        @NotBlank(message = "Field name cannot be blank")
        private String fieldName;

        @Field(type = FieldType.Keyword)
        @NotBlank(message = "Field type cannot be blank")
        private String fieldType;
    }

    @Data
    public static class LLMConfig {
        @Field(type = FieldType.Keyword)
        @NotBlank(message = "AI Model cannot be blank")
        private String aiModel;

        @Field(type = FieldType.Float)
        private Double temperature;

        @Field(type = FieldType.Integer)
        private Integer max_tokens;

        @Field(type = FieldType.Boolean)
        private Boolean stream;
    }
}
