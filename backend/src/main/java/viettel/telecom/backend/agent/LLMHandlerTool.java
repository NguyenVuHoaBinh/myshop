package viettel.telecom.backend.agent;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import viettel.telecom.backend.service.flow.LLMHandler;

import java.util.Map;

/**
 * Wraps LLMHandler for the ReAct agent.
 */
@Component
public class LLMHandlerTool implements AgentTool {

    private final LLMHandler llmHandler;

    public LLMHandlerTool(LLMHandler llmHandler) {
        this.llmHandler = llmHandler;
    }

    @Override
    public String getToolName() {
        return "LLM_HANDLER";
    }

    @Override
    public ToolResult executeTool(Map<String, Object> context) {
        WebSocketSession session = (WebSocketSession) context.get("session");
        Object nodeObj = context.get("node");

        // Call the real LLMHandler logic:
        String llmResponse;
        try {
            llmResponse = llmHandler.handle(null, context, session);
        } catch (Exception e) {
            llmResponse = "LLMHandler error: " + e.getMessage();
        }
        return new ToolResult("LLM Handler response: " + llmResponse);
    }
}
