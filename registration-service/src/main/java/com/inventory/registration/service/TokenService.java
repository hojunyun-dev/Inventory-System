package com.inventory.registration.service;

import com.inventory.registration.dto.TokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {
    
    private final WebClient.Builder webClientBuilder;
    
    @Value("${token-service.base-url}")
    private String tokenServiceBaseUrl;
    
    public TokenResponse getToken(String platform) {
        log.info("Getting token for platform: {}", platform);
        
        try {
            return webClientBuilder.build()
                .get()
                .uri(tokenServiceBaseUrl + "/api/tokens/{platform}", platform)
                .retrieve()
                .bodyToMono(TokenResponse.class)
                .block();
        } catch (Exception e) {
            log.error("Failed to get token for platform: {}, error: {}", platform, e.getMessage());
            throw new RuntimeException("Failed to get token: " + e.getMessage());
        }
    }
    
    public String getAccessToken(String platform) {
        TokenResponse tokenResponse = getToken(platform);
        if (tokenResponse == null || tokenResponse.getAccessToken() == null) {
            throw new RuntimeException("No valid token available for platform: " + platform);
        }
        return tokenResponse.getAccessToken();
    }
}

