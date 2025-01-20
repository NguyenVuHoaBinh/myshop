package viettel.telecom.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.WebSocketSession;
import viettel.telecom.backend.entity.flow.Flow;
import viettel.telecom.backend.service.flow.FlowExecutor;
import viettel.telecom.backend.service.flow.FlowService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final FlowService flowService;
    private final FlowExecutor flowExecutor;

    // In-memory map storing contexts for demonstration (per flowId or per user).
    // A production system would likely store contexts per session or in a DB.
    private final Map<String, Map<String, Object>> flowContexts = new HashMap<>();

    public ChatController(FlowService flowService, FlowExecutor flowExecutor) {
        this.flowService = flowService;
        this.flowExecutor = flowExecutor;
    }

    /**
     * Execute one step in the flow. If there's no current node,
     * we skip the start node and set the next node as current.
     * Then we process that current node exactly once.
     */
    @PostMapping("/{flowId}/execute")
    public ResponseEntity<?> executeFlow(
            @PathVariable String flowId,
            @RequestParam(required = false) String userResponse
    ) {
        try {
            // 1) Fetch the flow
            Flow flow = flowService.getFlow(flowId);
            if (flow == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Flow not found"));
            }

            // 2) Retrieve or initialize context
            Map<String, Object> context = flowContexts.computeIfAbsent(flowId, k -> new HashMap<>());

            // If the user provided a response, store it
            if (userResponse != null) {
                context.put("userResponse", userResponse);
            }

            // 3) Check if we already have a currentNodeId
            String currentNodeId = (String) context.get("currentNodeId");

            if (currentNodeId == null) {
                // -- Skip the start node --
                Flow.Node startNode = flow.getNodes().stream()
                        .filter(n -> "startNode".equals(n.getType()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Start node not found"));

                // Find the next node after startNode
                String nextNodeId = flow.getEdges().stream()
                        .filter(e -> e.getSource().equals(startNode.getId()))
                        .map(Flow.Edge::getTarget)
                        .findFirst()
                        .orElse(null);

                if (nextNodeId == null) {
                    return ResponseEntity.ok(Map.of("message", "No node after start node. Flow halted."));
                }

                // We don't process it immediately; we set it as current
                currentNodeId = nextNodeId;
                context.put("currentNodeId", currentNodeId);

                return ResponseEntity.ok(Map.of(
                        "message", "Flow is ready at the next node. Please submit userResponse again to process.",
                        "currentNodeId", currentNodeId
                ));
            }

            // 4) Process the current node using FlowExecutor
            String finalCurrentNodeId = currentNodeId;
            Flow.Node currentNode = flow.getNodes().stream()
                    .filter(n -> n.getId().equals(finalCurrentNodeId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Node not found: " + finalCurrentNodeId));

            // Passing null for WebSocketSession. This is an HTTP call.
            String nextNodeId = flowExecutor.processNode(flow, currentNode, context, null);

            if (nextNodeId != null) {
                context.put("currentNodeId", nextNodeId);
                return ResponseEntity.ok(Map.of(
                        "message", "Node processed successfully",
                        "currentNodeId", nextNodeId
                ));
            } else {
                // Flow ended
                context.remove("currentNodeId");
                return ResponseEntity.ok(Map.of("message", "Flow execution completed."));
            }

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
