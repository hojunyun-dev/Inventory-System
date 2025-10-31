package com.inventory.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/automation")
public class AutomationProxyController {

    @Value("${registration-service.url}")
    private String registrationServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/bunjang/session/open")
    public ResponseEntity<String> openSession() {
        try {
            String url = registrationServiceUrl + "/api/automation/bunjang/session/open";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"success\":false,\"message\":\"Failed to open session: " + e.getMessage() + "\"}");
        }
    }
    
    @PostMapping("/bunjang/session/open-with-product")
    public ResponseEntity<String> openSessionWithProduct(@RequestBody String requestBody) {
        try {
            String url = registrationServiceUrl + "/api/automation/bunjang/session/open-with-product";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"success\":false,\"message\":\"Failed to open session with product: " + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/bunjang/session/status")
    public ResponseEntity<String> getSessionStatus() {
        try {
            String url = registrationServiceUrl + "/api/automation/bunjang/session/status";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"success\":false,\"message\":\"Failed to get session status: " + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/bunjang/session/close")
    public ResponseEntity<String> closeSession() {
        try {
            String url = registrationServiceUrl + "/api/automation/bunjang/session/close";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"success\":false,\"message\":\"Failed to close session: " + e.getMessage() + "\"}");
        }
    }

}

