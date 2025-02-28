package viettel.telecom.backend.agent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import viettel.telecom.backend.service.llm.ReActAgent;

import java.util.HashMap;
import java.util.Map;

/**
 * A demo controller exposing an endpoint that triggers the ReActAgent flow.
 */
@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final ReActAgent reActAgent;

    /**
     * Constructor injection of the ReActAgent.
     * Spring will automatically wire the ReActAgent bean.
     */
    @Autowired
    public AgentController(ReActAgent reActAgent) {
        this.reActAgent = reActAgent;
    }

    /**
     * Example endpoint:
     * POST /api/agent/ask
     * Body (JSON): { "query": "User question..." }
     *
     * Returns JSON: { "answer": "Agent's final answer" }
     */
    @PostMapping("/ask")
    public ResponseEntity<Map<String, Object>> askAgent(@RequestBody Map<String, String> body) {
        // Extract the user query from the request body
        String userQuery = body.getOrDefault("query", "No query provided");

        // Build a context for the agent (could store session info, node data, etc.)
        Map<String, Object> context = new HashMap<>();
        // e.g. context.put("session", someWebSocketSession);

        // Run the agent
        String answer = reActAgent.runAgent(userQuery, context);

        // Return the final answer in JSON form
        return ResponseEntity.ok(Map.of("answer", answer));
    }
}
