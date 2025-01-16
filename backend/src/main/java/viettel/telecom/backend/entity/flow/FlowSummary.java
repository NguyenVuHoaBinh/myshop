package viettel.telecom.backend.entity.flow;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Minimal fields for listing flows (lightweight).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlowSummary {
    private String id;
    private String name;
    private String description;
}

