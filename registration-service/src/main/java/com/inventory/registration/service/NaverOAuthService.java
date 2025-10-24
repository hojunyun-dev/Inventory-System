package com.inventory.registration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NaverOAuthService {
    
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    
    @Value("${oauth.naver.client-id:}")
    private String clientId;
    
    @Value("${oauth.naver.client-secret:}")
    private String clientSecret;
    
    @Value("${oauth.naver.redirect-uri:http://localhost:8082/api/oauth/naver/callback}")
    private String redirectUri;
    
    @Value("${oauth.naver.authorize-url:https://nid.naver.com/oauth2.0/authorize}")
    private String authorizeUrl;
    
    @Value("${oauth.naver.token-url:https://nid.naver.com/oauth2.0/token}")
    private String tokenUrl;
    
    @Value("${oauth.naver.user-info-url:https://openapi.naver.com/v1/nid/me}")
    private String userInfoUrl;
    
    /**
     * 네이버 OAuth 인증 URL 생성
     */
    public String generateAuthUrl(String state) {
        return authorizeUrl + "?" +
                "response_type=code&" +
                "client_id=" + clientId + "&" +
                "redirect_uri=" + redirectUri + "&" +
                "state=" + state;
    }
    
    /**
     * 인증 코드로 액세스 토큰 발급
     */
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> getAccessToken(String code, String state) {
        log.info("네이버 OAuth 액세스 토큰 요청 시작");
        
        return webClientBuilder.build()
                .post()
                .uri(tokenUrl)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .bodyValue("grant_type=authorization_code&" +
                        "client_id=" + clientId + "&" +
                        "client_secret=" + clientSecret + "&" +
                        "code=" + code + "&" +
                        "state=" + state)
                .retrieve()
                .bodyToMono(Map.class)
                .map(result -> (Map<String, Object>) result)
                .doOnSuccess(response -> {
                    log.info("네이버 OAuth 액세스 토큰 발급 성공: {}", response.get("access_token"));
                })
                .doOnError(error -> {
                    log.error("네이버 OAuth 액세스 토큰 발급 실패: {}", error.getMessage());
                });
    }
    
    /**
     * 액세스 토큰으로 사용자 정보 조회
     */
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> getUserInfo(String accessToken) {
        log.info("네이버 사용자 정보 조회 시작");
        
        return webClientBuilder.build()
                .get()
                .uri(userInfoUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .map(result -> (Map<String, Object>) result)
                .doOnSuccess(response -> {
                    log.info("네이버 사용자 정보 조회 성공: {}", response);
                })
                .doOnError(error -> {
                    log.error("네이버 사용자 정보 조회 실패: {}", error.getMessage());
                });
    }
    
    /**
     * 토큰 갱신
     */
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> refreshToken(String refreshToken) {
        log.info("네이버 OAuth 토큰 갱신 시작");
        
        return webClientBuilder.build()
                .post()
                .uri(tokenUrl)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .bodyValue("grant_type=refresh_token&" +
                        "client_id=" + clientId + "&" +
                        "client_secret=" + clientSecret + "&" +
                        "refresh_token=" + refreshToken)
                .retrieve()
                .bodyToMono(Map.class)
                .map(result -> (Map<String, Object>) result)
                .doOnSuccess(response -> {
                    log.info("네이버 OAuth 토큰 갱신 성공");
                })
                .doOnError(error -> {
                    log.error("네이버 OAuth 토큰 갱신 실패: {}", error.getMessage());
                });
    }
    
    /**
     * 토큰 폐기
     */
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> revokeToken(String accessToken) {
        log.info("네이버 OAuth 토큰 폐기 시작");
        
        String revokeUrl = "https://nid.naver.com/oauth2.0/token";
        
        return webClientBuilder.build()
                .post()
                .uri(revokeUrl)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .bodyValue("grant_type=delete&" +
                        "client_id=" + clientId + "&" +
                        "client_secret=" + clientSecret + "&" +
                        "access_token=" + accessToken + "&" +
                        "service_provider=NAVER")
                .retrieve()
                .bodyToMono(Map.class)
                .map(result -> (Map<String, Object>) result)
                .doOnSuccess(response -> {
                    log.info("네이버 OAuth 토큰 폐기 성공");
                })
                .doOnError(error -> {
                    log.error("네이버 OAuth 토큰 폐기 실패: {}", error.getMessage());
                });
    }
}
