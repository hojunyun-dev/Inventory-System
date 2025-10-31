package com.inventory.registration.controller;

import com.inventory.registration.dto.ProductRegistrationRequest;
import com.inventory.registration.dto.ProductRegistrationResponse;
import com.inventory.registration.dto.RegistrationStatusResponse;
import com.inventory.registration.service.ProductRegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/registrations")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ProductRegistrationController {
    
    private final ProductRegistrationService registrationService;
    
    @PostMapping
    public ResponseEntity<Map<String, String>> registerProduct(@Valid @RequestBody ProductRegistrationRequest request) {
        log.info("POST /api/registrations - platform: {}, product: {}", request.getPlatform(), request.getProductName());
        
        try {
            registrationService.registerProduct(request);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Product registration started successfully");
            response.put("platform", request.getPlatform());
            response.put("productId", request.getProductId());
            response.put("status", "PENDING");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to register product: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to register product: " + e.getMessage());
        }
    }
    
    @GetMapping("/platform/{platform}")
    public ResponseEntity<List<ProductRegistrationResponse>> getRegistrationsByPlatform(@PathVariable String platform) {
        log.info("GET /api/registrations/platform/{}", platform);
        
        try {
            List<ProductRegistrationResponse> registrations = registrationService.getRegistrationsByPlatform(platform);
            return ResponseEntity.ok(registrations);
        } catch (Exception e) {
            log.error("Failed to get registrations for platform: {}, error: {}", platform, e.getMessage());
            throw new RuntimeException("Failed to get registrations: " + e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ProductRegistrationResponse> getRegistrationById(@PathVariable Long id) {
        log.info("GET /api/registrations/{}", id);
        
        try {
            ProductRegistrationResponse registration = registrationService.getRegistrationById(id);
            return ResponseEntity.ok(registration);
        } catch (Exception e) {
            log.error("Failed to get registration by id: {}, error: {}", id, e.getMessage());
            throw new RuntimeException("Failed to get registration: " + e.getMessage());
        }
    }
    
    @GetMapping("/status/{platform}")
    public ResponseEntity<RegistrationStatusResponse> getRegistrationStatus(@PathVariable String platform) {
        log.info("GET /api/registrations/status/{}", platform);
        
        try {
            RegistrationStatusResponse status = registrationService.getRegistrationStatus(platform);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Failed to get registration status for platform: {}, error: {}", platform, e.getMessage());
            throw new RuntimeException("Failed to get registration status: " + e.getMessage());
        }
    }
    
    @GetMapping("/retryable")
    public ResponseEntity<List<ProductRegistrationResponse>> getRetryableRegistrations() {
        log.info("GET /api/registrations/retryable");
        
        try {
            List<ProductRegistrationResponse> registrations = registrationService.getRetryableRegistrations();
            return ResponseEntity.ok(registrations);
        } catch (Exception e) {
            log.error("Failed to get retryable registrations: {}", e.getMessage());
            throw new RuntimeException("Failed to get retryable registrations: " + e.getMessage());
        }
    }
    
    @PostMapping("/{id}/retry")
    public ResponseEntity<ProductRegistrationResponse> retryRegistration(@PathVariable Long id) {
        log.info("POST /api/registrations/{}/retry", id);
        
        try {
            ProductRegistrationResponse response = registrationService.retryRegistration(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to retry registration: {}, error: {}", id, e.getMessage());
            throw new RuntimeException("Failed to retry registration: " + e.getMessage());
        }
    }
}
