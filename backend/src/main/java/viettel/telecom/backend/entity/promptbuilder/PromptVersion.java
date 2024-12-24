package viettel.telecom.backend.entity.promptbuilder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "prompt_versions")
public class PromptVersion {
    @Id
    private String id;
    private String promptId;
    private String version;
    private Map<String, Object> promptConfig;
    private boolean isActive;
}

