package viettel.telecom.backend.entity.llm;

import java.util.Map;

public interface LLMModel {
    String generateText(String prompt, Map<String, Object> parameters);
}
