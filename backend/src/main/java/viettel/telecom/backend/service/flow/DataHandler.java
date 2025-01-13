package viettel.telecom.backend.service.flow;

import org.springframework.stereotype.Service;
import viettel.telecom.backend.entity.flow.Flow;

import java.util.Map;

@Service
public class DataHandler {

    public String handle(Flow.Step step, Map<String, Object> context) {
        try {
            Map<String, Object> dataConfig = (Map<String, Object>) step.getLlmConfig();
            String key = (String) dataConfig.get("key");
            String value = (String) dataConfig.get("value");

            context.put(key, value); // Update context with the data
            return step.getNextStepId();
        } catch (Exception e) {
            return step.getFallbackStepId(); // Move to fallback on failure
        }
    }
}
