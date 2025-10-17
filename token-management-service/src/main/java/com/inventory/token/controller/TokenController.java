package com.inventory.token.controller;

import com.inventory.token.dto.TokenRequest;
import com.inventory.token.dto.TokenResponse;
import com.inventory.token.service.OAuthTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/tokens")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TokenController {
    
    private final OAuthTokenService tokenService;
    
    @GetMapping("/{platform}")
    public ResponseEntity<TokenResponse> getToken(@PathVariable String platform) {
        log.info("GET /api/tokens/{}", platform);
        
        try {
            TokenResponse response = tokenService.getToken(platform);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get token for platform: {}, error: {}", platform, e.getMessage());
            throw new RuntimeException("Failed to get token for platform: " + platform + ", error: " + e.getMessage());
        }
    }
    
    @PostMapping
    public ResponseEntity<TokenResponse> issueToken(@Valid @RequestBody TokenRequest request) {
        log.info("POST /api/tokens - platform: {}", request.getPlatform());
        
        try {
            TokenResponse response = tokenService.issueToken(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to issue token for platform: {}, error: {}", request.getPlatform(), e.getMessage());
            throw new RuntimeException("Failed to issue token for platform: " + request.getPlatform() + ", error: " + e.getMessage());
        }
    }
    
    @PostMapping("/{platform}/refresh")
    public ResponseEntity<TokenResponse> refreshToken(@PathVariable String platform, @RequestParam String refreshToken) {
        log.info("POST /api/tokens/{}/refresh", platform);
        
        try {
            TokenResponse response = tokenService.refreshToken(platform, refreshToken);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to refresh token for platform: {}, error: {}", platform, e.getMessage());
            throw new RuntimeException("Failed to refresh token for platform: " + platform + ", error: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{platform}")
    public ResponseEntity<Void> revokeToken(@PathVariable String platform) {
        log.info("DELETE /api/tokens/{}", platform);
        
        try {
            // Deactivate all tokens for the platform
            tokenService.revokeToken(platform);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to revoke token for platform: {}, error: {}", platform, e.getMessage());
            throw new RuntimeException("Failed to revoke token for platform: " + platform + ", error: " + e.getMessage());
        }
    }
}
