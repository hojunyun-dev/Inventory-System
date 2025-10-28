package com.inventory.registration.service.bunjang;

import com.example.common.dto.CookieEntry;
import com.example.common.dto.TokenBundle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
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

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, TokenBundle> tokenCache = new ConcurrentHashMap<>();

    /**
     * TokenBundle 저장
     */
    public void saveTokenBundle(TokenBundle tokenBundle) {
        try {
            log.info("💾 Saving token bundle for platform: {}", tokenBundle.platform);
            
            // 메모리 캐시에 저장
            tokenCache.put(tokenBundle.platform, tokenBundle);
            
            // 파일 시스템에 저장
            saveToFile(tokenBundle);
            
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
            
            // 2. 파일에서 로드
            TokenBundle loaded = loadFromFile(platform);
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


