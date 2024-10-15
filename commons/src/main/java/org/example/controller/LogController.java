package org.example.controller;

import org.example.services.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class LogController {

    @Autowired
    private LogService logService;

    @GetMapping
    public ResponseEntity<List<String>> getAllLogs() {
        List<String> logs = logService.getAllLogs();
        return ResponseEntity.ok(logs);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearLogs() {
        logService.clearLogs();
        return ResponseEntity.noContent().build();
    }
}
