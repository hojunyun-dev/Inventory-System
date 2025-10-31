package com.inventory.registration.service.bunjang;

import com.example.common.dto.TokenBundle;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TokenBundle 영속화 서비스
 * - 메모리 캐시 + 파일 시스템 백업
 * - JSON 직렬화/역직렬화
 * - 만료 시간 관리
 */
@Service
@Slf4j
public class TokenBundleService {

    @Value("${token.storage.path:./tokens}")
    private String tokenStoragePath;
    
    @Value("${token.management.service.url:http://localhost:8083}")
    private String tokenManagementServiceUrl;

    @Value("${token.management.enable:true}")
    private boolean enableDb;

    @Value("${token.storage.enableFile:false}")
    private boolean enableFile;

    private final ObjectMapper objectMapper;
    private final Map<String, TokenBundle> tokenCache = new ConcurrentHashMap<>();
    private final RestTemplate restTemplate;
    
    public TokenBundleService() {
        this.objectMapper = new ObjectMapper();
        // JSR310 모듈 등록 (java.time.Instant 지원)
        this.objectMapper.registerModule(new JavaTimeModule());
        this.restTemplate = new RestTemplate();
        log.info("✅ ObjectMapper configured with JSR310 module for java.time support");
    }

    /**
     * TokenBundle 저장
     */
    public void saveTokenBundle(TokenBundle tokenBundle) {
        try {
            log.info("💾 Saving token bundle for platform: {}", tokenBundle.platform);
            
            // 메모리 캐시에 저장
            tokenCache.put(tokenBundle.platform, tokenBundle);
            
            // 파일 시스템에 저장 (옵션)
            if (enableFile) {
                saveToFile(tokenBundle);
            }
            
            // 토큰 관리 서비스에 저장 (DB)
            if (enableDb) {
                saveToTokenManagementService(tokenBundle);
            }
            
            log.info("✅ Token bundle saved successfully");
        } catch (Exception e) {
            log.error("❌ Failed to save token bundle: {}", e.getMessage(), e);
        }
    }

    /**
     * TokenBundle 조회
     */
    public TokenBundle getTokenBundle(String platform) {
        try {
            log.info("🔍 Retrieving token bundle for platform: {}", platform);
            
            // 1. 메모리 캐시에서 조회
            TokenBundle cached = tokenCache.get(platform);
            if (cached != null && !isExpired(cached)) {
                log.info("✅ Token bundle found in cache");
                return cached;
            }
            
            // 2. 토큰 관리 서비스(DB)에서 조회 시도
            if (enableDb) {
                TokenBundle dbLoaded = loadFromTokenManagementService(platform);
                if (dbLoaded != null && !isExpired(dbLoaded)) {
                    log.info("✅ Token bundle loaded from token-management-service");
                    tokenCache.put(platform, dbLoaded);
                    return dbLoaded;
                }
            }

            // 3. 파일에서 로드 (옵션)
            TokenBundle loaded = enableFile ? loadFromFile(platform) : null;
            if (loaded != null && !isExpired(loaded)) {
                log.info("✅ Token bundle loaded from file");
                tokenCache.put(platform, loaded);
                return loaded;
            }
            
            log.warn("⚠️ No valid token bundle found for platform: {}", platform);
            return null;
        } catch (Exception e) {
            log.error("❌ Failed to retrieve token bundle: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 토큰 만료 여부 확인
     */
    public boolean isExpired(TokenBundle tokenBundle) {
        if (tokenBundle.expiresAt == null) {
            return false; // 만료 시간이 없으면 만료되지 않은 것으로 간주
        }
        return Instant.now().isAfter(tokenBundle.expiresAt);
    }

    /**
     * 토큰 삭제
     */
    public void deleteTokenBundle(String platform) {
        try {
            log.info("🗑️ Deleting token bundle for platform: {}", platform);
            
            // 메모리에서 삭제
            tokenCache.remove(platform);
            
            // 파일에서 삭제
            deleteFile(platform);
            
            log.info("✅ Token bundle deleted successfully");
        } catch (Exception e) {
            log.error("❌ Failed to delete token bundle: {}", e.getMessage(), e);
        }
    }

    /**
     * 파일에 저장
     */
    private void saveToFile(TokenBundle tokenBundle) throws IOException {
        Path storageDir = Paths.get(tokenStoragePath);
        if (!Files.exists(storageDir)) {
            Files.createDirectories(storageDir);
        }
        
        Path tokenFile = storageDir.resolve(tokenBundle.platform + ".json");
        String json = objectMapper.writeValueAsString(tokenBundle);
        Files.write(tokenFile, json.getBytes());
        
        log.debug("💾 Token bundle saved to file: {}", tokenFile);
    }

    /**
     * 파일에서 로드
     */
    private TokenBundle loadFromFile(String platform) throws IOException {
        Path tokenFile = Paths.get(tokenStoragePath, platform + ".json");
        if (!Files.exists(tokenFile)) {
            return null;
        }
        
        String json = new String(Files.readAllBytes(tokenFile));
        TokenBundle tokenBundle = objectMapper.readValue(json, TokenBundle.class);
        
        log.debug("📂 Token bundle loaded from file: {}", tokenFile);
        return tokenBundle;
    }

    /**
     * 파일 삭제
     */
    private void deleteFile(String platform) throws IOException {
        Path tokenFile = Paths.get(tokenStoragePath, platform + ".json");
        if (Files.exists(tokenFile)) {
            Files.delete(tokenFile);
            log.debug("🗑️ Token file deleted: {}", tokenFile);
        }
    }

    /**
     * 토큰 관리 서비스에 저장
     */
    private void saveToTokenManagementService(TokenBundle tokenBundle) {
        try {
            log.info("💾 Saving token to token management service...");
            
            // Direct 저장 엔드포인트 페이로드 구성
            Map<String, Object> tokenRequest = new HashMap<>();
            tokenRequest.put("platform", tokenBundle.platform.toLowerCase());
            tokenRequest.put("accessToken", tokenBundle.authToken);
            tokenRequest.put("refreshToken", tokenBundle.csrf); // CSRF를 refreshToken으로 저장
            tokenRequest.put("tokenType", "Bearer");
            tokenRequest.put("scope", "bunjang_api");
            if (tokenBundle.expiresAt != null) {
                LocalDateTime expiresAt = LocalDateTime.ofInstant(tokenBundle.expiresAt, java.time.ZoneId.systemDefault());
                tokenRequest.put("expiresAt", expiresAt.toString());
            } else {
                tokenRequest.put("expiresIn", 32400);
            }
            
            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // 요청 엔티티 생성
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(tokenRequest, headers);
            
            // 토큰 관리 서비스에 POST 요청
            String url = tokenManagementServiceUrl + "/api/tokens/direct";
            ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(url, requestEntity, (Class<Map<String, Object>>)(Class)Map.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ Token saved to token management service successfully");
            } else {
                log.warn("⚠️ Token management service returned status: {}", response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("❌ Failed to save token to token management service: {}", e.getMessage(), e);
            // 토큰 관리 서비스 저장 실패해도 로컬 저장은 계속 진행
        }
    }

    /**
     * 토큰 관리 서비스에서 조회하여 TokenBundle로 변환
     */
    private TokenBundle loadFromTokenManagementService(String platform) {
        try {
            String url = tokenManagementServiceUrl + "/api/tokens/direct/" + platform.toLowerCase();
            ResponseEntity<Map<String, Object>> response = restTemplate.getForEntity(url, (Class<Map<String, Object>>)(Class)Map.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return null;
            }
            Map<String, Object> body = response.getBody();
            TokenBundle tb = new TokenBundle();
            tb.platform = (String) body.getOrDefault("platform", platform);
            tb.authToken = (String) body.get("accessToken");
            tb.csrf = (String) body.get("refreshToken");
            Object expiresAtStr = body.get("expiresAt");
            if (expiresAtStr instanceof String s && !s.isBlank()) {
                java.time.LocalDateTime ldt = java.time.LocalDateTime.parse(s);
                tb.expiresAt = ldt.atZone(java.time.ZoneId.systemDefault()).toInstant();
            }
            tb.cookies = new java.util.ArrayList<>();
            return tb;
        } catch (Exception e) {
            log.warn("⚠️ Failed to load token from token-management-service: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 모든 토큰 상태 조회
     */
    public Map<String, Object> getAllTokenStatus() {
        Map<String, Object> status = new ConcurrentHashMap<>();
        
        for (Map.Entry<String, TokenBundle> entry : tokenCache.entrySet()) {
            String platform = entry.getKey();
            TokenBundle tokenBundle = entry.getValue();
            
            Map<String, Object> platformStatus = new ConcurrentHashMap<>();
            platformStatus.put("hasToken", tokenBundle != null);
            platformStatus.put("isExpired", tokenBundle != null && isExpired(tokenBundle));
            platformStatus.put("cookieCount", tokenBundle != null ? tokenBundle.cookies.size() : 0);
            platformStatus.put("hasCsrf", tokenBundle != null && tokenBundle.csrf != null);
            platformStatus.put("expiresAt", tokenBundle != null ? tokenBundle.expiresAt : null);
            
            status.put(platform, platformStatus);
        }
        
        return status;
    }
}


