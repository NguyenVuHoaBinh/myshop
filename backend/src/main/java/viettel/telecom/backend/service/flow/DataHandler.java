package viettel.telecom.backend.service.flow;

import org.springframework.stereotype.Service;
import viettel.telecom.backend.entity.flow.Flow;

import java.util.Map;

@Service
public class DataHandler {

    public String handle(Flow.Node node, Map<String, Object> context) {
        try {
            Map<String, Object> llmConfig = (Map<String, Object>) node.getData().getSelectedTemplate();
            String key = (String) llmConfig.get("key");
            String value = (String) llmConfig.get("value");

            context.put(key, value); // Update context with data
            return node.getData().getTemplateId();
        } catch (Exception e) {
            return node.getData().getLabel(); // Return fallback step ID on failure
        }
    }
}
