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

    public ChatController(FlowService flowService, FlowExecutor flowExecutor) {
        this.flowService = flowService;
        this.flowExecutor = flowExecutor;
    }

    @PostMapping("/{flowId}/execute")
    public ResponseEntity<?> executeFlow(
            @PathVariable String flowId,
            @RequestParam(required = false) String userResponse
    ) {
        try {
            // Fetch the flow
            Flow flow = flowService.getFlow(flowId);
            if (flow == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Flow not found"));
            }

            // Retrieve or initialize context
            Map<String, Object> context = new HashMap<>();
            if (userResponse != null) {
                context.put("userResponse", userResponse);
            }

            // Determine the current node
            String currentNodeId = (String) context.getOrDefault("currentNodeId", null);
            if (currentNodeId == null) {
                // Start the flow
                flowExecutor.executeFlow(flow, context, null);
                currentNodeId = flow.getNodes().stream()
                        .filter(node -> "startNode".equals(node.getType()))
                        .map(Flow.Node::getId)
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Start node not found"));

                context.put("currentNodeId", currentNodeId);
            }

            // Process the current node
            String finalCurrentNodeId = currentNodeId;
            Flow.Node currentNode = flow.getNodes().stream()
                    .filter(node -> node.getId().equals(finalCurrentNodeId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Node not found"));

            String nextNodeId = flowExecutor.processNode(flow, currentNode, context, null);

            if (nextNodeId != null) {
                context.put("currentNodeId", nextNodeId);
                return ResponseEntity.ok(Map.of(
                        "message", "Response processed successfully",
                        "nextNodeId", nextNodeId
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                        "message", "Flow execution completed"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }
}
