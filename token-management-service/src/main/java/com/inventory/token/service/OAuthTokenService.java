package com.inventory.token.service;

import com.inventory.token.dto.TokenRequest;
import com.inventory.token.dto.TokenResponse;
import com.inventory.token.entity.PlatformToken;
import com.inventory.token.repository.PlatformTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OAuthTokenService {
    
    private final PlatformTokenRepository tokenRepository;
    private final WebClient.Builder webClientBuilder;
    
    @Value("${oauth.naver.token-url}")
    private String naverTokenUrl;
    
    @Value("${oauth.cafe24.token-url}")
    private String cafe24TokenUrl;
    
    @Value("${oauth.coupang.token-url}")
    private String coupangTokenUrl;
    
    public TokenResponse getToken(String platform) {
        log.info("Getting token for platform: {}", platform);
        
        // Check if valid token exists
        Optional<PlatformToken> existingToken = tokenRepository.findValidTokenByPlatform(
            platform, LocalDateTime.now()
        );
        
        if (existingToken.isPresent()) {
            log.info("Valid token found for platform: {}", platform);
            return convertToResponse(existingToken.get());
        }
        
        // Try to refresh token if refresh token exists
        Optional<PlatformToken> tokenWithRefresh = tokenRepository.findByPlatformAndIsActiveTrue(platform);
        if (tokenWithRefresh.isPresent() && tokenWithRefresh.get().getRefreshToken() != null) {
            try {
                return refreshToken(platform, tokenWithRefresh.get().getRefreshToken());
            } catch (Exception e) {
                log.warn("Failed to refresh token for platform: {}, error: {}", platform, e.getMessage());
            }
        }
        
        throw new RuntimeException("No valid token available for platform: " + platform);
    }
    
    public TokenResponse issueToken(TokenRequest request) {
        log.info("Issuing new token for platform: {}", request.getPlatform());
        
        try {
            Map<String, Object> tokenData = requestTokenFromProvider(request);
            
            // Deactivate existing tokens
            tokenRepository.findByPlatformAndIsActiveTrue(request.getPlatform())
                .ifPresent(token -> {
                    token.setIsActive(false);
                    tokenRepository.save(token);
                });
            
            // Save new token
            PlatformToken newToken = createTokenFromResponse(request.getPlatform(), tokenData);
            PlatformToken savedToken = tokenRepository.save(newToken);
            
            log.info("Token issued successfully for platform: {}", request.getPlatform());
            return convertToResponse(savedToken);
            
        } catch (Exception e) {
            log.error("Failed to issue token for platform: {}, error: {}", request.getPlatform(), e.getMessage());
            throw new RuntimeException("Failed to issue token: " + e.getMessage());
        }
    }
    
    public TokenResponse refreshToken(String platform, String refreshToken) {
        log.info("Refreshing token for platform: {}", platform);
        
        try {
            // Get existing token to get client credentials
            Optional<PlatformToken> existingToken = tokenRepository.findByPlatformAndIsActiveTrue(platform);
            if (existingToken.isEmpty()) {
                throw new RuntimeException("No existing token found for platform: " + platform);
            }
            
            Map<String, Object> tokenData = refreshTokenFromProvider(platform, refreshToken);
            
            // Update existing token
            PlatformToken token = existingToken.get();
            updateTokenFromResponse(token, tokenData);
            PlatformToken savedToken = tokenRepository.save(token);
            
            log.info("Token refreshed successfully for platform: {}", platform);
            return convertToResponse(savedToken);
            
        } catch (Exception e) {
            log.error("Failed to refresh token for platform: {}, error: {}", platform, e.getMessage());
            throw new RuntimeException("Failed to refresh token: " + e.getMessage());
        }
    }
    
    private Map<String, Object> requestTokenFromProvider(TokenRequest request) {
        String tokenUrl = getTokenUrl(request.getPlatform());
        
        // OAuth 표준에 따라 form-urlencoded 형태로 전송
        StringBuilder formData = new StringBuilder();
        formData.append("grant_type=").append(request.getGrantType());
        formData.append("&client_id=").append(request.getClientId());
        formData.append("&client_secret=").append(request.getClientSecret());
        if (request.getScope() != null) {
            formData.append("&scope=").append(request.getScope());
        }
        
        return webClientBuilder.build()
            .post()
            .uri(tokenUrl)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .bodyValue(formData.toString())
            .retrieve()
            .bodyToMono(Map.class)
            .block();
    }
    
    private Map<String, Object> refreshTokenFromProvider(String platform, String refreshToken) {
        String tokenUrl = getTokenUrl(platform);
        
        // OAuth 표준에 따라 form-urlencoded 형태로 전송
        StringBuilder formData = new StringBuilder();
        formData.append("grant_type=refresh_token");
        formData.append("&refresh_token=").append(refreshToken);
        
        return webClientBuilder.build()
            .post()
            .uri(tokenUrl)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .bodyValue(formData.toString())
            .retrieve()
            .bodyToMono(Map.class)
            .block();
    }
    
    private String getTokenUrl(String platform) {
        return switch (platform.toLowerCase()) {
            case "naver" -> naverTokenUrl;
            case "cafe24" -> cafe24TokenUrl;
            case "coupang" -> coupangTokenUrl;
            default -> throw new IllegalArgumentException("Unsupported platform: " + platform);
        };
    }
    
    private PlatformToken createTokenFromResponse(String platform, Map<String, Object> tokenData) {
        PlatformToken token = new PlatformToken();
        token.setPlatform(platform);
        token.setAccessToken((String) tokenData.get("access_token"));
        token.setRefreshToken((String) tokenData.get("refresh_token"));
        token.setTokenType((String) tokenData.get("token_type"));
        token.setScope((String) tokenData.get("scope"));
        
        // Calculate expiry time
        Integer expiresIn = (Integer) tokenData.get("expires_in");
        if (expiresIn != null) {
            token.setExpiresAt(LocalDateTime.now().plusSeconds(expiresIn));
        }
        
        token.setIsActive(true);
        return token;
    }
    
    private void updateTokenFromResponse(PlatformToken token, Map<String, Object> tokenData) {
        token.setAccessToken((String) tokenData.get("access_token"));
        if (tokenData.get("refresh_token") != null) {
            token.setRefreshToken((String) tokenData.get("refresh_token"));
        }
        token.setTokenType((String) tokenData.get("token_type"));
        token.setScope((String) tokenData.get("scope"));
        
        // Calculate expiry time
        Integer expiresIn = (Integer) tokenData.get("expires_in");
        if (expiresIn != null) {
            token.setExpiresAt(LocalDateTime.now().plusSeconds(expiresIn));
        }
    }
    
    public void revokeToken(String platform) {
        log.info("Revoking token for platform: {}", platform);
        
        tokenRepository.findByPlatformAndIsActiveTrue(platform)
            .ifPresent(token -> {
                token.setIsActive(false);
                tokenRepository.save(token);
                log.info("Token revoked successfully for platform: {}", platform);
            });
    }
    
    private TokenResponse convertToResponse(PlatformToken token) {
        TokenResponse response = new TokenResponse();
        response.setPlatform(token.getPlatform());
        response.setAccessToken(token.getAccessToken());
        response.setTokenType(token.getTokenType());
        response.setScope(token.getScope());
        response.setIsActive(token.getIsActive());
        response.setCreatedAt(token.getCreatedAt());
        
        if (token.getExpiresAt() != null) {
            response.setExpiresAt(token.getExpiresAt());
            response.setExpiresIn(java.time.Duration.between(LocalDateTime.now(), token.getExpiresAt()).getSeconds());
        }
        
        return response;
    }
}
