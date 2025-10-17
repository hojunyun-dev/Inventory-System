package com.inventory.service;

import com.inventory.dto.TokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class TokenManagementService {

    private final WebClient tokenServiceWebClient;
    
    public TokenManagementService(@Qualifier("tokenServiceWebClient") WebClient tokenServiceWebClient) {
        this.tokenServiceWebClient = tokenServiceWebClient;
    }

    @Value("${token-management-service.url:http://localhost:8081}")
    private String tokenManagementServiceUrl;

    /**
     * 플랫폼별 토큰 조회
     */
    public Mono<TokenResponse> getToken(String platform) {
        log.info("Getting token for platform: {}", platform);
        
        return tokenServiceWebClient.get()
                .uri(tokenManagementServiceUrl + "/api/tokens/" + platform)
                .retrieve()
                .bodyToMono(TokenResponse.class)
                .doOnNext(token -> log.info("Successfully retrieved token for platform: {}", platform))
                .doOnError(e -> log.error("Failed to get token for platform {}: {}", platform, e.getMessage()));
    }

    /**
     * 토큰 갱신
     */
    public Mono<TokenResponse> refreshToken(String platform) {
        log.info("Refreshing token for platform: {}", platform);
        
        return tokenServiceWebClient.post()
                .uri(tokenManagementServiceUrl + "/api/tokens/" + platform + "/refresh")
                .retrieve()
                .bodyToMono(TokenResponse.class)
                .doOnNext(token -> log.info("Successfully refreshed token for platform: {}", platform))
                .doOnError(e -> log.error("Failed to refresh token for platform {}: {}", platform, e.getMessage()));
    }
}

