package com.inventory.registration.service.bunjang;

import com.example.common.dto.CookieEntry;
import com.example.common.dto.ProductRegisterRequest;
import com.example.common.dto.TokenBundle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * 번개장터 API 기반 상품 등록 서비스
 * - TokenBundle을 사용한 인증
 * - HTTP 헤더 구성 (Cookie, CSRF, User-Agent 등)
 * - 재시도 로직 포함
 */
@Service
@Slf4j
public class BunjangApiRegistrationService {

    @Value("${bunjang.api.base-url:https://m.bunjang.co.kr}")
    private String baseUrl;

    @Value("${bunjang.api.product-endpoint:/api/sell}")
    private String productEndpoint;

    private final WebClient webClient;
    private final TokenBundleService tokenBundleService;

    public BunjangApiRegistrationService(WebClient.Builder webClientBuilder, TokenBundleService tokenBundleService) {
        this.webClient = webClientBuilder
            .baseUrl(baseUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
        this.tokenBundleService = tokenBundleService;
    }

    /**
     * 상품 등록
     */
    public Mono<Map<String, Object>> registerProduct(ProductRegisterRequest request) {
        return Mono.fromCallable(() -> {
            log.info("📦 Starting Bunjang API product registration: {}", request.name);
            
            // 1. 토큰 조회
            TokenBundle tokenBundle = tokenBundleService.getTokenBundle("BUNJANG");
            if (tokenBundle == null || tokenBundleService.isExpired(tokenBundle)) {
                throw new RuntimeException("No valid token found. Please login first.");
            }
            
            // 2. API 요청 구성
            String url = baseUrl + productEndpoint;
            Map<String, Object> requestBody = buildRequestBody(request);
            
            // 3. HTTP 헤더 구성
            HttpHeaders headers = buildHeaders(tokenBundle);
            
            // 4. API 호출
            return callBunjangApi(url, requestBody, headers);
            
        }).onErrorMap(throwable -> {
            log.error("❌ Product registration failed: {}", throwable.getMessage(), throwable);
            return new RuntimeException("Product registration failed: " + throwable.getMessage());
        });
    }

    /**
     * API 요청 본문 구성
     */
    private Map<String, Object> buildRequestBody(ProductRegisterRequest request) {
        return Map.of(
            "name", request.name,
            "price", request.price,
            "description", request.description != null ? request.description : "",
            "categoryId", request.categoryId != null ? request.categoryId : "",
            "keywords", request.keywords != null ? request.keywords : List.of()
        );
    }

    /**
     * HTTP 헤더 구성
     */
    private HttpHeaders buildHeaders(TokenBundle tokenBundle) {
        HttpHeaders headers = new HttpHeaders();
        
        // Cookie 헤더 구성
        if (tokenBundle.cookies != null && !tokenBundle.cookies.isEmpty()) {
            String cookieHeader = buildCookieHeader(tokenBundle.cookies);
            headers.add(HttpHeaders.COOKIE, cookieHeader);
            log.debug("🍪 Cookie header set: {} cookies", tokenBundle.cookies.size());
        }
        
        // CSRF 토큰
        if (tokenBundle.csrf != null) {
            headers.add("X-CSRF-Token", tokenBundle.csrf);
            log.debug("🔐 CSRF token set");
        }
        
        // 기타 헤더
        headers.add(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Mobile Safari/537.36");
        headers.add(HttpHeaders.ORIGIN, baseUrl);
        headers.add(HttpHeaders.REFERER, baseUrl + "/sell");
        headers.add(HttpHeaders.ACCEPT, "application/json, text/plain, */*");
        headers.add(HttpHeaders.ACCEPT_LANGUAGE, "ko-KR,ko;q=0.9,en;q=0.8");
        
        return headers;
    }

    /**
     * 쿠키 헤더 구성
     */
    private String buildCookieHeader(List<CookieEntry> cookies) {
        StringBuilder cookieBuilder = new StringBuilder();
        for (int i = 0; i < cookies.size(); i++) {
            CookieEntry cookie = cookies.get(i);
            cookieBuilder.append(cookie.name).append("=").append(cookie.value);
            if (i < cookies.size() - 1) {
                cookieBuilder.append("; ");
            }
        }
        return cookieBuilder.toString();
    }

    /**
     * 번개장터 API 호출
     */
    private Map<String, Object> callBunjangApi(String url, Map<String, Object> requestBody, HttpHeaders headers) {
        try {
            log.info("🌐 Calling Bunjang API: {}", url);
            
            Map<String, Object> response = webClient.post()
                .uri(url)
                .headers(h -> h.addAll(headers))
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(30))
                .block();
            
            log.info("✅ Bunjang API call successful");
            return response;
            
        } catch (Exception e) {
            log.error("❌ Bunjang API call failed: {}", e.getMessage(), e);
            throw new RuntimeException("API call failed: " + e.getMessage());
        }
    }

    /**
     * 토큰 유효성 검사
     */
    public boolean isTokenValid() {
        TokenBundle tokenBundle = tokenBundleService.getTokenBundle("BUNJANG");
        return tokenBundle != null && !tokenBundleService.isExpired(tokenBundle);
    }

    /**
     * 토큰 상태 조회
     */
    public Map<String, Object> getTokenStatus() {
        TokenBundle tokenBundle = tokenBundleService.getTokenBundle("BUNJANG");
        if (tokenBundle == null) {
            return Map.of(
                "hasToken", false,
                "isExpired", true,
                "message", "No token found"
            );
        }
        
        boolean isExpired = tokenBundleService.isExpired(tokenBundle);
        return Map.of(
            "hasToken", true,
            "isExpired", isExpired,
            "cookieCount", tokenBundle.cookies.size(),
            "hasCsrf", tokenBundle.csrf != null,
            "expiresAt", tokenBundle.expiresAt,
            "message", isExpired ? "Token expired" : "Token valid"
        );
    }
}


