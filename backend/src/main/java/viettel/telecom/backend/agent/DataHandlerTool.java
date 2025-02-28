package viettel.telecom.backend.agent;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import viettel.telecom.backend.entity.flow.Flow;
import viettel.telecom.backend.service.flow.DataHandler;

import java.util.Map;

/**
 * Wraps DataHandler so the ReActAgent can call it as a "tool."
 * This class implements the AgentTool interface, which defines
 * how the ReActAgent can invoke external functionalities (tools).
 */
@Component
public class DataHandlerTool implements AgentTool {

    private final DataHandler dataHandler;

    /**
     * Spring will inject the DataHandler bean here automatically.
     */
    public DataHandlerTool(DataHandler dataHandler) {
        this.dataHandler = dataHandler;
    }

    @Override
    public String getToolName() {
        // The name by which the ReActAgent (or other code) identifies this tool
        return "DATA_HANDLER";
    }

    @Override
    public ToolResult executeTool(Map<String, Object> context) {
        /**
         * In a real scenario, you might parse the Flow.Node or other parameters
         * from the context Map. For example:
         *     Flow.Node node = (Flow.Node) context.get("node");
         *     WebSocketSession session = (WebSocketSession) context.get("session");
         */
        Flow.Node node = (Flow.Node) context.get("node");
        WebSocketSession session = (WebSocketSession) context.get("session");

        if (node == null) {
            // If no node is provided, we can’t call DataHandler properly
            return new ToolResult("No node provided in context for DataHandlerTool");
        }

        // Call your existing DataHandler logic.
        // The 'handle' method typically returns a String that may indicate the next node ID.
        String nextNodeId;
        try {
            nextNodeId = dataHandler.handle(node, context, session);
        } catch (Exception e) {
            // Catch any runtime errors and wrap them in a ToolResult
            return new ToolResult("DataHandler error: " + e.getMessage());
        }

        // Return a ToolResult that captures what happened
        return new ToolResult("DataHandler handled node " + node.getId() +
                " => next node: " + nextNodeId);
    }
}

