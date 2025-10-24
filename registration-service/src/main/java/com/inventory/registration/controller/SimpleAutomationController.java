package com.inventory.registration.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/automation")
@Slf4j
public class SimpleAutomationController {
    
    /**
     * 지원되는 플랫폼 목록 조회
     */
    @GetMapping("/platforms")
    public ResponseEntity<List<String>> getSupportedPlatforms() {
        log.info("Getting supported platforms");
        List<String> platforms = Arrays.asList("bunjang", "danggeun", "junggonara");
        return ResponseEntity.ok(platforms);
    }
    
    /**
     * 플랫폼 상태 확인
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getPlatformStatus() {
        log.info("Getting platform status");
        Map<String, Object> status = new HashMap<>();
        status.put("bunjang", "Available");
        status.put("danggeun", "Available (SMS verification required)");
        status.put("junggonara", "Available");
        status.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(status);
    }
    
    /**
     * 테스트용 엔드포인트
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test() {
        log.info("Test endpoint called");
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Automation service is working");
        response.put("status", "OK");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
    
    /**
     * 상품 데이터 파싱 테스트
     */
    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> testRegistration(@RequestBody Map<String, Object> request) {
        log.info("Test registration request received: {}", request);
        
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Product data parsed successfully");
            response.put("receivedData", request);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Test registration failed: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}






