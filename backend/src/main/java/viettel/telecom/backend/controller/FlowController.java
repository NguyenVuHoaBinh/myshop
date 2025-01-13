package viettel.telecom.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.WebSocketSession;
import viettel.telecom.backend.entity.flow.Flow;
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
    private final LogManagementService logManagementService;

    @Autowired
    public FlowController(FlowService flowService, FlowExecutor flowExecutor, LogManagementService logManagementService) {
        this.flowService = flowService;
        this.flowExecutor = flowExecutor;
        this.logManagementService = logManagementService;
    }

    // Create a new flow
    @PostMapping
    public ResponseEntity<Map<String, Object>> createFlow(@RequestBody Flow flow) {
        try {
            String flowId = flowService.createFlow(flow);
            Map<String, Object> response = new HashMap<>();
            response.put("flowId", flowId);
            response.put("message", "Flow created successfully");
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    // Update an existing flow
    @PutMapping("/{flowId}")
    public ResponseEntity<Map<String, Object>> updateFlow(@PathVariable String flowId, @RequestBody Flow updatedFlow) {
        try {
            if (!flowId.equals(updatedFlow.getId())) {
                throw new IllegalArgumentException("Path flowId and payload flowId do not match");
            }
            flowService.createFlow(updatedFlow); // Replace the existing flow with the updated one
            Map<String, Object> response = new HashMap<>();
            response.put("flowId", flowId);
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

    // Retrieve a flow by its ID
    @GetMapping("/{flowId}")
    public ResponseEntity<Map<String, Object>> getFlow(@PathVariable String flowId) {
        try {
            Flow flow = flowService.getFlow(flowId);
            return ResponseEntity.ok(Map.of(
                    "flowId", flow.getId(),
                    "flowDetails", flow,
                    "timestamp", LocalDateTime.now()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "error", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    // Delete a flow by its ID
    @DeleteMapping("/{flowId}")
    public ResponseEntity<Map<String, Object>> deleteFlow(@PathVariable String flowId) {
        try {
            flowService.deleteFlowById(flowId);
            return ResponseEntity.ok(Map.of(
                    "flowId", flowId,
                    "message", "Flow deleted successfully",
                    "timestamp", LocalDateTime.now()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    // Execute a flow by its ID
    @PostMapping("/{flowId}/execute")
    public ResponseEntity<Map<String, Object>> executeFlow(
            @PathVariable String flowId,
            @RequestBody Map<String, Object> initialContext,
            WebSocketSession session) {
        try {
            flowExecutor.executeFlow(flowId, initialContext, session);
            return ResponseEntity.ok(Map.of(
                    "flowId", flowId,
                    "message", "Flow execution started successfully",
                    "timestamp", LocalDateTime.now()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    // Retrieve logs for a flow execution
    @GetMapping("/{flowId}/logs")
    public ResponseEntity<Map<String, Object>> getExecutionLogs(
            @PathVariable String flowId,
            @RequestParam(required = false, defaultValue = "INFO") String logLevel) {
        try {
            List<String> logs = logManagementService.getLogs(logLevel);
            return ResponseEntity.ok(Map.of(
                    "flowId", flowId,
                    "logs", logs,
                    "timestamp", LocalDateTime.now()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }
}
