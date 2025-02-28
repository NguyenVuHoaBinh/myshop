package viettel.telecom.backend.agent;

/**
 * Simple wrapper for a tool's result.
 * Could be extended to include status codes, errors, etc.
 */
public class ToolResult {
    private final String output;

    public ToolResult(String output) {
        this.output = output;
    }

    public String getOutput() {
        return output;
    }
}
