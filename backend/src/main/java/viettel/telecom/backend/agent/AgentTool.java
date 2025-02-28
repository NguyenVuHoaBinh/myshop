package viettel.telecom.backend.agent;


import java.util.Map;

/**
 * Interface for any "Tool" the ReAct agent can call.
 * Each tool is responsible for a specific capability:
 * - Data fetching
 * - Flow execution
 * - LLM invocation
 * - etc.
 */
public interface AgentTool {
    /**
     * The unique name or ID of this tool, e.g. "DATA_HANDLER".
     */
    String getToolName();

    /**
     * Execute the tool with the provided context (which may include parameters).
     * Returns a ToolResult object containing the outcome or content.
     */
    ToolResult executeTool(Map<String, Object> context);
}

