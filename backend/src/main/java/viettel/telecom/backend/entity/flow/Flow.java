package viettel.telecom.backend.entity.flow;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
public class Flow {

    @NotBlank(message = "Flow ID cannot be blank")
    private String id;

    @NotBlank(message = "Flow name cannot be blank")
    private String name;

    @NotBlank(message = "Role cannot be blank")
    private String role;

    @NotBlank(message = "Purpose cannot be blank")
    private String purpose;

    @NotEmpty(message = "Steps cannot be empty")
    private List<Step> steps;

    @NotBlank(message = "CreatedBy cannot be blank")
    private String createdBy;

    @NotBlank(message = "CreatedAt cannot be blank")
    private String createdAt;

    private String updatedAt;

    @Data
    public static class Step {
        @NotBlank(message = "Step ID cannot be blank")
        private String id;

        @NotBlank(message = "Template ID cannot be blank")
        private String templateId;

        @NotBlank(message = "Action type cannot be blank")
        private String actionType; // interaction, llm, logic, data

        private String condition; // Optional, only for logic steps

        private String prompt;    // Optional, only for interaction steps

        private String nextStepId;

        private String fallbackStepId;

        private Object llmConfig; // Optional, for LLM-specific configuration
    }
}
