package org.example.services;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class LogService {

    private final List<String> logs = Collections.synchronizedList(new ArrayList<>());

    public void addLog(String log) {
        logs.add(anonymizeLog(log));
    }

    public List<String> getAllLogs() {
        return new ArrayList<>(logs);
    }

    public void clearLogs() {
        logs.clear();
    }

    private String anonymizeLog(String log) {
        // Replace any occurrence of IDs with a placeholder
        return log.replaceAll("\\d+", "[ID]");
    }
}
