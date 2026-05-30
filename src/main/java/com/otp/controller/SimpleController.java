package com.otp.controller;

import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class SimpleController {
    
    private final Map<String, OtpData> otpStorage = new ConcurrentHashMap<>();
    private final Random random = new Random();
    
    @GetMapping("/")
    public Map<String, String> root() {
        Map<String, String> response = new HashMap<>();
        response.put("service", "OTP Service");
        response.put("status", "running");
        response.put("version", "1.0.0");
        return response;
    }
    
    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "OK");
        status.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return status;
    }
    
    @PostMapping("/api/auth/register")
    public Map<String, String> register(@RequestBody Map<String, String> user) {
        String login = user.get("login");
        String password = user.get("password");
        String role = user.getOrDefault("role", "USER");
        
        System.out.println("User registered: " + login);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "User registered successfully");
        response.put("login", login);
        response.put("role", role);
        return response;
    }
    
    @PostMapping("/api/auth/login")
    public Map<String, String> login(@RequestBody Map<String, String> credentials) {
        String login = credentials.get("login");
        String password = credentials.get("password");
        
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI" + 
                       Base64.getEncoder().encodeToString(login.getBytes()) + 
                       "Iiwicm9sZSI6IlVTRVIifQ.fake-signature";
        
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("role", "USER");
        return response;
    }
    
    @PostMapping("/api/otp/generate")
    public Map<String, String> generateOtp(@RequestBody Map<String, String> request,
                                            @RequestHeader(value = "Authorization", required = false) String auth) {
        String operationId = UUID.randomUUID().toString();
        String code = String.format("%06d", random.nextInt(1000000));
        String channel = request.getOrDefault("channel", "EMAIL");
        String destination = request.getOrDefault("destination", "test@example.com");
        
        otpStorage.put(operationId, new OtpData(code, System.currentTimeMillis() + 300000));
        
        System.out.println("========================================");
        System.out.println("OTP GENERATED:");
        System.out.println("Operation ID: " + operationId);
        System.out.println("Code: " + code);
        System.out.println("Channel: " + channel);
        System.out.println("Destination: " + destination);
        System.out.println("Auth: " + (auth != null ? "Bearer token present" : "No auth"));
        System.out.println("========================================");
        
        Map<String, String> response = new HashMap<>();
        response.put("operationId", operationId);
        response.put("code", code);
        response.put("channel", channel);
        response.put("destination", destination);
        response.put("message", "OTP generated (check console for code)");
        return response;
    }
    
    @PostMapping("/api/otp/validate")
    public Map<String, Boolean> validateOtp(@RequestBody Map<String, String> request) {
        String operationId = request.get("operationId");
        String code = request.get("code");
        
        OtpData otpData = otpStorage.get(operationId);
        boolean isValid = otpData != null && 
                         otpData.code.equals(code) && 
                         otpData.expiresAt > System.currentTimeMillis();
        
        if (isValid) {
            otpStorage.remove(operationId);
            System.out.println("OTP VALIDATED: " + operationId);
        } else {
            System.out.println("OTP FAILED: " + operationId);
        }
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("valid", isValid);
        return response;
    }
    
    @GetMapping("/api/otp/test")
    public Map<String, String> testGenerate() {
        String operationId = UUID.randomUUID().toString();
        String code = String.format("%06d", random.nextInt(1000000));
        otpStorage.put(operationId, new OtpData(code, System.currentTimeMillis() + 300000));
        
        Map<String, String> response = new HashMap<>();
        response.put("operationId", operationId);
        response.put("code", code);
        return response;
    }
    
    private static class OtpData {
        final String code;
        final long expiresAt;
        
        OtpData(String code, long expiresAt) {
            this.code = code;
            this.expiresAt = expiresAt;
        }
    }
}