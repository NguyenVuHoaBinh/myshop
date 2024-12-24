package viettel.telecom.backend.entity.promptbuilder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "prompt_templates")
public class PromptTemplate {
    @Id
    private String id;
    private String type;
    private String name;
    private String apiName;
}

