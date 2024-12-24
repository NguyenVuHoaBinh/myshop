package viettel.telecom.backend.entity.ingestion;

import java.util.HashMap;
import java.util.Map;

public class IngestionResult {
    private String message;
    private Map<String, String> failures = new HashMap<>();
    private Map<String, String> warnings = new HashMap<>();
    private Map<String, Object> details = new HashMap<>();

    // Getters and setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, String> getFailures() {
        return failures;
    }

    public void setFailures(Map<String, String> failures) {
        this.failures = failures;
    }

    public Map<String, String> getWarnings() {
        return warnings;
    }

    public void setWarnings(Map<String, String> warnings) {
        this.warnings = warnings;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }
}
