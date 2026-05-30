package com.otp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class FileNotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(FileNotificationService.class);
    private static final String OTP_FILE = "otp_codes.log";
    
    public void saveToFile(String operationId, String code, String destination, String channel) {
        try {
            Path filePath = Paths.get(OTP_FILE);
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
                try (PrintWriter writer = new PrintWriter(new FileWriter(OTP_FILE, true))) {
                    writer.println("=== OTP Codes Log ===");
                    writer.println("Created: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    writer.println("========================================");
                }
            }
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(OTP_FILE, true))) {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                writer.printf("[%s] Operation: %s | Channel: %s | Destination: %s | Code: %s%n",
                    timestamp, operationId, channel, destination, code);
                writer.flush();
            }
            
            logger.info("OTP code saved to file: {} for operation: {}", OTP_FILE, operationId);
        } catch (IOException e) {
            logger.error("Failed to save OTP code to file: {}", e.getMessage());
            throw new RuntimeException("Failed to save OTP code to file", e);
        }
    }
}