package com.inventory.token.controller;

import com.inventory.token.dto.AccountRequest;
import com.inventory.token.dto.AccountResponse;
import com.inventory.token.service.AccountManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AccountController {
    
    private final AccountManagementService accountService;
    
    @GetMapping("/{platform}")
    public ResponseEntity<List<AccountResponse>> getAccountsByPlatform(@PathVariable String platform) {
        log.info("GET /api/accounts/{}", platform);
        
        try {
            List<AccountResponse> accounts = accountService.getAccountsByPlatform(platform);
            return ResponseEntity.ok(accounts);
        } catch (Exception e) {
            log.error("Failed to get accounts for platform: {}, error: {}", platform, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{platform}/{username}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable String platform, @PathVariable String username) {
        log.info("GET /api/accounts/{}/{}", platform, username);
        
        try {
            AccountResponse account = accountService.getAccount(platform, username);
            return ResponseEntity.ok(account);
        } catch (Exception e) {
            log.error("Failed to get account for platform: {}, username: {}, error: {}", 
                platform, username, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody AccountRequest request) {
        log.info("POST /api/accounts - platform: {}, username: {}", request.getPlatform(), request.getUsername());
        
        try {
            AccountResponse account = accountService.createAccount(request);
            return ResponseEntity.ok(account);
        } catch (Exception e) {
            log.error("Failed to create account for platform: {}, username: {}, error: {}", 
                request.getPlatform(), request.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{platform}/{username}")
    public ResponseEntity<AccountResponse> updateAccount(@PathVariable String platform, 
                                                       @PathVariable String username,
                                                       @Valid @RequestBody AccountRequest request) {
        log.info("PUT /api/accounts/{}/{}", platform, username);
        
        try {
            AccountResponse account = accountService.updateAccount(platform, username, request);
            return ResponseEntity.ok(account);
        } catch (Exception e) {
            log.error("Failed to update account for platform: {}, username: {}, error: {}", 
                platform, username, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{platform}/{username}")
    public ResponseEntity<Void> deleteAccount(@PathVariable String platform, @PathVariable String username) {
        log.info("DELETE /api/accounts/{}/{}", platform, username);
        
        try {
            accountService.deleteAccount(platform, username);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to delete account for platform: {}, username: {}, error: {}", 
                platform, username, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{platform}/unlocked")
    public ResponseEntity<List<AccountResponse>> getUnlockedAccounts(@PathVariable String platform) {
        log.info("GET /api/accounts/{}/unlocked", platform);
        
        try {
            List<AccountResponse> accounts = accountService.getUnlockedAccounts(platform);
            return ResponseEntity.ok(accounts);
        } catch (Exception e) {
            log.error("Failed to get unlocked accounts for platform: {}, error: {}", platform, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}

