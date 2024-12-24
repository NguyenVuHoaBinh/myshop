package viettel.telecom.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import viettel.telecom.backend.service.logging.LogManagementService;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class LogManagementController {

    private final LogManagementService logManagementService;

    @Autowired
    public LogManagementController(LogManagementService logManagementService) {
        this.logManagementService = logManagementService;
    }

    /**
     * Fetches logs based on the specified log level.
     *
     * @param level The log level to filter by (optional).
     * @return A list of logs.
     */
    @GetMapping
    public ResponseEntity<List<String>> getLogs(@RequestParam(required = false) String level) {
        List<String> logs = logManagementService.getLogs(level);
        return ResponseEntity.ok(logs);
    }
}

