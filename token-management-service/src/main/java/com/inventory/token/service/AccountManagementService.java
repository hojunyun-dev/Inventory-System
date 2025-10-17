package com.inventory.token.service;

import com.inventory.token.dto.AccountRequest;
import com.inventory.token.dto.AccountResponse;
import com.inventory.token.entity.PlatformAccount;
import com.inventory.token.repository.PlatformAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AccountManagementService {
    
    private final PlatformAccountRepository accountRepository;
    private final EncryptionService encryptionService;
    
    public AccountResponse getAccount(String platform, String username) {
        log.info("Getting account for platform: {}, username: {}", platform, username);
        
        Optional<PlatformAccount> account = accountRepository.findByPlatformAndUsernameAndIsActiveTrue(platform, username);
        if (account.isEmpty()) {
            throw new RuntimeException("Account not found for platform: " + platform + ", username: " + username);
        }
        
        return convertToResponse(account.get());
    }
    
    public List<AccountResponse> getAccountsByPlatform(String platform) {
        log.info("Getting all accounts for platform: {}", platform);
        
        List<PlatformAccount> accounts = accountRepository.findByPlatformAndIsActiveTrue(platform);
        return accounts.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    public AccountResponse createAccount(AccountRequest request) {
        log.info("Creating account for platform: {}, username: {}", request.getPlatform(), request.getUsername());
        
        // Check if account already exists
        Optional<PlatformAccount> existingAccount = accountRepository.findByPlatformAndUsernameAndIsActiveTrue(
            request.getPlatform(), request.getUsername()
        );
        
        if (existingAccount.isPresent()) {
            throw new RuntimeException("Account already exists for platform: " + request.getPlatform() + 
                ", username: " + request.getUsername());
        }
        
        // Create new account
        PlatformAccount account = new PlatformAccount();
        account.setPlatform(request.getPlatform());
        account.setUsername(request.getUsername());
        account.setEncryptedPassword(encryptionService.encrypt(request.getPassword()));
        account.setTwoFactorSecret(request.getTwoFactorSecret());
        account.setIsActive(true);
        account.setLoginAttempts(0);
        
        PlatformAccount savedAccount = accountRepository.save(account);
        log.info("Account created successfully for platform: {}, username: {}", 
            request.getPlatform(), request.getUsername());
        
        return convertToResponse(savedAccount);
    }
    
    public AccountResponse updateAccount(String platform, String username, AccountRequest request) {
        log.info("Updating account for platform: {}, username: {}", platform, username);
        
        Optional<PlatformAccount> accountOpt = accountRepository.findByPlatformAndUsernameAndIsActiveTrue(platform, username);
        if (accountOpt.isEmpty()) {
            throw new RuntimeException("Account not found for platform: " + platform + ", username: " + username);
        }
        
        PlatformAccount account = accountOpt.get();
        if (request.getPassword() != null) {
            account.setEncryptedPassword(encryptionService.encrypt(request.getPassword()));
        }
        if (request.getTwoFactorSecret() != null) {
            account.setTwoFactorSecret(request.getTwoFactorSecret());
        }
        
        PlatformAccount savedAccount = accountRepository.save(account);
        log.info("Account updated successfully for platform: {}, username: {}", platform, username);
        
        return convertToResponse(savedAccount);
    }
    
    public void deleteAccount(String platform, String username) {
        log.info("Deleting account for platform: {}, username: {}", platform, username);
        
        Optional<PlatformAccount> accountOpt = accountRepository.findByPlatformAndUsernameAndIsActiveTrue(platform, username);
        if (accountOpt.isEmpty()) {
            throw new RuntimeException("Account not found for platform: " + platform + ", username: " + username);
        }
        
        PlatformAccount account = accountOpt.get();
        account.setIsActive(false);
        accountRepository.save(account);
        
        log.info("Account deleted successfully for platform: {}, username: {}", platform, username);
    }
    
    public String getDecryptedPassword(String platform, String username) {
        log.info("Getting decrypted password for platform: {}, username: {}", platform, username);
        
        Optional<PlatformAccount> account = accountRepository.findByPlatformAndUsernameAndIsActiveTrue(platform, username);
        if (account.isEmpty()) {
            throw new RuntimeException("Account not found for platform: " + platform + ", username: " + username);
        }
        
        return encryptionService.decrypt(account.get().getEncryptedPassword());
    }
    
    public void updateLoginAttempts(String platform, String username, int attempts) {
        log.info("Updating login attempts for platform: {}, username: {}, attempts: {}", 
            platform, username, attempts);
        
        Optional<PlatformAccount> accountOpt = accountRepository.findByPlatformAndUsernameAndIsActiveTrue(platform, username);
        if (accountOpt.isPresent()) {
            PlatformAccount account = accountOpt.get();
            account.setLoginAttempts(attempts);
            if (attempts >= 5) { // Lock account after 5 failed attempts
                account.setLockedUntil(LocalDateTime.now().plusHours(1));
            }
            accountRepository.save(account);
        }
    }
    
    public void updateLastLogin(String platform, String username) {
        log.info("Updating last login for platform: {}, username: {}", platform, username);
        
        Optional<PlatformAccount> accountOpt = accountRepository.findByPlatformAndUsernameAndIsActiveTrue(platform, username);
        if (accountOpt.isPresent()) {
            PlatformAccount account = accountOpt.get();
            account.setLastLogin(LocalDateTime.now());
            account.setLoginAttempts(0); // Reset login attempts on successful login
            account.setLockedUntil(null); // Unlock account on successful login
            accountRepository.save(account);
        }
    }
    
    public List<AccountResponse> getUnlockedAccounts(String platform) {
        log.info("Getting unlocked accounts for platform: {}", platform);
        
        List<PlatformAccount> accounts = accountRepository.findUnlockedAccountsByPlatform(platform, LocalDateTime.now());
        return accounts.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    private AccountResponse convertToResponse(PlatformAccount account) {
        AccountResponse response = new AccountResponse();
        response.setPlatform(account.getPlatform());
        response.setUsername(account.getUsername());
        response.setIsActive(account.getIsActive());
        response.setLastLogin(account.getLastLogin());
        response.setLoginAttempts(account.getLoginAttempts());
        response.setLockedUntil(account.getLockedUntil());
        response.setCreatedAt(account.getCreatedAt());
        return response;
    }
}

