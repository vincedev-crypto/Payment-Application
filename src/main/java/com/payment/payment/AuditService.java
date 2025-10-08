package com.payment.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort; // <-- NEW IMPORT
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditService {

    @Autowired
    private LogRepository logRepository;

    public void logAction(String action, String details, String user, String sourceIp) {
        Log log = new Log(action, details, user, sourceIp);
        logRepository.save(log);
    }

    public List<Log> getAllLogs() {
        // FINAL FIX: Sort by timestamp in descending order (newest first)
        return logRepository.findAll(Sort.by(Sort.Direction.DESC, "timestamp"));
    }
}