package viettel.telecom.backend.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.WebSocketSession;
import viettel.telecom.backend.entity.flow.Flow;
import viettel.telecom.backend.service.flow.FlowService;
import viettel.telecom.backend.service.logging.LogManagementService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/flows")
public class FlowController {

    private final FlowService flowService;
    private final LogManagementService logManagementService;

    @Autowired
    public FlowController(FlowService flowService, LogManagementService logManagementService) {
        this.flowService = flowService;
        this.logManagementService = logManagementService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllFlows() {
        try {
            List<Flow> flows = flowService.getAllFlows();
            Map<String, Object> response = new HashMap<>();
            response.put("flows", flows);
            response.put("message", "All flows retrieved successfully");
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    @PostMapping
    public ResponseEntity<Map<String, Object>> createFlow(@Valid @RequestBody Flow flow) {
        try {
            String flowId = flowService.createFlow(flow);
            Map<String, Object> response = new HashMap<>();
            response.put("flowId", flowId);
            response.put("message", "Flow created successfully");
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/{flowId}")
    public ResponseEntity<Map<String, Object>> updateFlow(@PathVariable String flowId, @Valid @RequestBody Flow updatedFlow) {
        try {
            if (!flowId.equals(updatedFlow.getId())) {
                throw new IllegalArgumentException("Path flowId and payload flowId do not match");
            }
            flowService.createFlow(updatedFlow); // Elasticsearch replaces existing document with same ID
            Map<String, Object> response = new HashMap<>();
            response.put("flowId", flowId);
            response.put("message", "Flow updated successfully");
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/{flowId}")
    public ResponseEntity<Map<String, Object>> getFlow(@PathVariable String flowId) {
        try {
            Flow flow = flowService.getFlow(flowId);
            Map<String, Object> response = new HashMap<>();
            response.put("flowId", flow.getId());
            response.put("flowDetails", flow);
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @DeleteMapping("/{flowId}")
    public ResponseEntity<Map<String, Object>> deleteFlow(@PathVariable String flowId) {
        try {
            flowService.deleteFlowById(flowId);
            Map<String, Object> response = new HashMap<>();
            response.put("flowId", flowId);
            response.put("message", "Flow deleted successfully");
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/{flowId}/execute")
    public ResponseEntity<Map<String, Object>> executeFlow(
            @PathVariable String flowId,
            @RequestBody Map<String, Object> initialContext,
            WebSocketSession session) {
        long startTime = System.currentTimeMillis();
        try {
            flowService.executeFlowById(flowId, initialContext, session);
            long executionTime = System.currentTimeMillis() - startTime;
            Map<String, Object> response = new HashMap<>();
            response.put("flowId", flowId);
            response.put("message", "Flow execution started successfully");
            response.put("executionTimeMs", executionTime);
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/{flowId}/logs")
    public ResponseEntity<Map<String, Object>> getExecutionLogs(
            @PathVariable String flowId,
            @RequestParam(required = false, defaultValue = "INFO") String logLevel) {
        try {
            List<String> logs = logManagementService.getLogs(logLevel);
            Map<String, Object> response = new HashMap<>();
            response.put("flowId", flowId);
            response.put("logs", logs);
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
