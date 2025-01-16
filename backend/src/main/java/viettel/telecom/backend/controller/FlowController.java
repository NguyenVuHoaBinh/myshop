package viettel.telecom.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.WebSocketSession;
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
    private final FlowExecutor flowExecutor;        // example usage
    private final LogManagementService logService;  // example usage

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
            // Fetch the created flow to return it in the response
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
            flowService.createFlow(updatedFlow);  // effectively upsert in many DBs
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
    // If you still want a route that returns the FULL flow objects:
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

    // ================ 7) EXECUTE (Optional) ================
    // Example method for flow execution
    @PostMapping("/{flowId}/execute")
    public ResponseEntity<?> executeFlow(
            @PathVariable String flowId,
            @RequestBody Map<String, Object> initialContext
    ) {
        try {
            WebSocketSession mockSession = null; // or your actual WebSocketSession
            Flow flow = flowService.getFlow(flowId);

            // If you have a FlowExecutor, call it:
            flowExecutor.executeFlow(flow, initialContext, mockSession);

            Map<String, Object> response = new HashMap<>();
            response.put("flow", flow);
            response.put("message", "Flow execution started successfully");
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.ok(response);

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
