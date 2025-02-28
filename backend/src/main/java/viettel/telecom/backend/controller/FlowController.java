package viettel.telecom.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import viettel.telecom.backend.entity.flow.Flow;
import viettel.telecom.backend.entity.flow.FlowSummary;
import viettel.telecom.backend.service.flow.FlowExecutor;
import viettel.telecom.backend.service.flow.FlowService;
import viettel.telecom.backend.service.logging.LogManagementService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/flows")
public class FlowController {

    private final FlowService flowService;
    private final FlowExecutor flowExecutor;
    private final LogManagementService logService;

    /**
     * Simple in-memory map to store context per flowId (demo only).
     * In a real system, you'd likely track context per user or session, not per flowId.
     */
    private final Map<String, Map<String, Object>> flowContexts = new HashMap<>();

    @Autowired
    public FlowController(FlowService flowService,
                          FlowExecutor flowExecutor,
                          LogManagementService logService) {
        this.flowService = flowService;
        this.flowExecutor = flowExecutor;
        this.logService = logService;
    }

    // ================ 1) CREATE  ================
    @PostMapping
    public ResponseEntity<?> createFlow(@RequestBody Flow flow) {
        try {
            String newFlowId = flowService.createFlow(flow);
            Flow createdFlow = flowService.getFlow(newFlowId);

            Map<String, Object> response = new HashMap<>();
            response.put("flow", createdFlow);
            response.put("message", "Flow created successfully");
            response.put("timestamp", LocalDateTime.now());
            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    // ================ 2) UPDATE  ================
    @PutMapping("/{flowId}")
    public ResponseEntity<?> updateFlow(@PathVariable String flowId, @RequestBody Flow updatedFlow) {
        try {
            if (!flowId.equals(updatedFlow.getId())) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Path flowId and payload flowId do not match",
                        "timestamp", LocalDateTime.now()
                ));
            }
            flowService.createFlow(updatedFlow);  // upsert approach
            Flow savedFlow = flowService.getFlow(flowId);

            Map<String, Object> response = new HashMap<>();
            response.put("flow", savedFlow);
            response.put("message", "Flow updated successfully");
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    // ================ 3) GET ALL - FULL  ================
    @GetMapping
    public ResponseEntity<?> getAllFlows(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            List<Flow> flows = flowService.listFlows(page, size);
            Map<String, Object> response = new HashMap<>();
            response.put("flows", flows);
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    // ================ 4) GET ALL - SUMMARY ONLY  ================
    @GetMapping("/summary")
    public ResponseEntity<?> getAllFlowSummaries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            List<FlowSummary> summaries = flowService.listFlowSummaries(page, size);
            Map<String, Object> response = new HashMap<>();
            response.put("flows", summaries);
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    // ================ 5) GET BY ID - FULL DETAILS  ================
    @GetMapping("/{flowId}")
    public ResponseEntity<?> getFlow(@PathVariable String flowId) {
        try {
            Flow flow = flowService.getFlow(flowId);
            Map<String, Object> response = new HashMap<>();
            response.put("flow", flow);
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "error", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    // ================ 6) DELETE  ================
    @DeleteMapping("/{flowId}")
    public ResponseEntity<?> deleteFlow(@PathVariable String flowId) {
        try {
            flowService.deleteFlowById(flowId);
            Map<String, Object> response = new HashMap<>();
            response.put("flowId", flowId);
            response.put("message", "Flow deleted successfully");
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    // ================ 7) EXECUTE - auto-run next node after start node ================
    @PostMapping("/{flowId}/execute")
    public ResponseEntity<?> executeFlow(
            @PathVariable String flowId,
            @RequestBody(required = false) Map<String, Object> requestBody
    ) {
        try {
            // 1) Fetch the flow
            Flow flow = flowService.getFlow(flowId);
            if (flow == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Flow not found"));
            }

            // 2) Retrieve or create flow-specific context
            Map<String, Object> context = flowContexts.computeIfAbsent(flowId, k -> new HashMap<>());

            // 3) If the user provided something (e.g. "userResponse"), store it
            if (requestBody != null && requestBody.get("userResponse") != null) {
                context.put("userResponse", requestBody.get("userResponse"));
            }

            // 4) Check if we already have a currentNodeId
            String currentNodeId = (String) context.get("currentNodeId");
            if (currentNodeId == null) {
                // ------------------------------------------------------------
                // Skip the start node, BUT auto-execute the next node
                // ------------------------------------------------------------
                Flow.Node startNode = flow.getNodes().stream()
                        .filter(n -> "startNode".equals(n.getType()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Start node not found"));

                // find the node after the start node
                String nextNodeId = flow.getEdges().stream()
                        .filter(e -> e.getSource().equals(startNode.getId()))
                        .map(Flow.Edge::getTarget)
                        .findFirst()
                        .orElse(null);

                if (nextNodeId == null) {
                    return ResponseEntity.ok(Map.of("message", "No node after start node. Flow halted."));
                }

                // Immediately process that next node
                Flow.Node nodeAfterStart = flow.getNodes().stream()
                        .filter(n -> n.getId().equals(nextNodeId))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Node after start not found"));

                // Single-step process the node after start
                String followingNodeId = flowExecutor.processNode(flow, nodeAfterStart, context, null);

                // If the node after start leads to another node, store it in context
                if (followingNodeId == null) {
                    // Flow ended right after the first node
                    return ResponseEntity.ok(Map.of(
                            "message", "Flow completed immediately after the first node",
                            "processedNodeId", nextNodeId
                    ));
                } else {
                    // We have a next node after the node-after-start
                    context.put("currentNodeId", followingNodeId);
                    return ResponseEntity.ok(Map.of(
                            "message", "Auto-executed node after start. Flow paused at node: " + followingNodeId,
                            "processedNodeId", nextNodeId,
                            "currentNodeId", followingNodeId
                    ));
                }
            }

            // ---------------------------------------------------------------
            // 5) If we already have a currentNodeId, process that node (1 step)
            // ---------------------------------------------------------------
            Flow.Node currentNode = flow.getNodes().stream()
                    .filter(n -> n.getId().equals(currentNodeId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Node not found: " + currentNodeId));

            // pass null for WebSocketSession
            String nextNodeId = flowExecutor.processNode(flow, currentNode, context, null);

            if (nextNodeId == null) {
                // Flow ended
                context.remove("currentNodeId");
                return ResponseEntity.ok(Map.of(
                        "message", "Flow completed at node: " + currentNodeId
                ));
            } else {
                context.put("currentNodeId", nextNodeId);
                return ResponseEntity.ok(Map.of(
                        "message", "Node processed successfully. Moved to node " + nextNodeId,
                        "previousNodeId", currentNodeId,
                        "currentNodeId", nextNodeId
                ));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }


    // ================ 8) LOGS (Optional) ================
    @GetMapping("/{flowId}/logs")
    public ResponseEntity<?> getExecutionLogs(
            @PathVariable String flowId,
            @RequestParam(required = false, defaultValue = "INFO") String logLevel
    ) {
        try {
            // Example usage
            List<String> logs = logService.getLogs(logLevel);

            Map<String, Object> response = new HashMap<>();
            response.put("flowId", flowId);
            response.put("logs", logs);
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }
}
