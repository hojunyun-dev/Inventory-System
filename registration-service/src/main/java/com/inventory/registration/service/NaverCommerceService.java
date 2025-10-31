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
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NaverCommerceService {
    
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    
    @Value("${oauth.naver.api-base-url:https://api.commerce.naver.com}")
    private String naverApiBaseUrl;
    
    @Value("${oauth.naver.client-id:test-client-id}")
    private String clientId;
    
    @Value("${oauth.naver.client-secret:test-client-secret}")
    private String clientSecret;
    
    /**
     * 네이버 커머스 상품 등록
     */
    public Mono<Map<String, Object>> registerProduct(Map<String, Object> productData, String accessToken) {
        log.info("네이버 커머스 상품 등록 시작: {}", productData.get("name"));
        
        return webClientBuilder.build()
                .post()
                .uri(naverApiBaseUrl + "/products")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(productData)
                .retrieve()
                .bodyToMono(Map.class)
                .map(result -> (Map<String, Object>) result)
                .doOnSuccess(response -> {
                    log.info("네이버 커머스 상품 등록 성공: {}", response);
                })
                .doOnError(error -> {
                    log.error("네이버 커머스 상품 등록 실패: {}", error.getMessage());
                });
    }
    
    /**
     * 네이버 커머스 상품 수정
     */
    public Mono<Map<String, Object>> updateProduct(String productId, Map<String, Object> productData, String accessToken) {
        log.info("네이버 커머스 상품 수정 시작: {}", productId);
        
        return webClientBuilder.build()
                .put()
                .uri(naverApiBaseUrl + "/products/" + productId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(productData)
                .retrieve()
                .bodyToMono(Map.class)
                .map(result -> (Map<String, Object>) result)
                .doOnSuccess(response -> {
                    log.info("네이버 커머스 상품 수정 성공: {}", response);
                })
                .doOnError(error -> {
                    log.error("네이버 커머스 상품 수정 실패: {}", error.getMessage());
                });
    }
    
    /**
     * 네이버 커머스 상품 삭제
     */
    public Mono<Map<String, Object>> deleteProduct(String productId, String accessToken) {
        log.info("네이버 커머스 상품 삭제 시작: {}", productId);
        
        return webClientBuilder.build()
                .delete()
                .uri(naverApiBaseUrl + "/products/" + productId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .map(result -> (Map<String, Object>) result)
                .doOnSuccess(response -> {
                    log.info("네이버 커머스 상품 삭제 성공: {}", response);
                })
                .doOnError(error -> {
                    log.error("네이버 커머스 상품 삭제 실패: {}", error.getMessage());
                });
    }
    
    /**
     * 네이버 커머스 상품 조회
     */
    public Mono<Map<String, Object>> getProduct(String productId, String accessToken) {
        log.info("네이버 커머스 상품 조회 시작: {}", productId);
        
        return webClientBuilder.build()
                .get()
                .uri(naverApiBaseUrl + "/products/" + productId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .map(result -> (Map<String, Object>) result)
                .doOnSuccess(response -> {
                    log.info("네이버 커머스 상품 조회 성공: {}", response);
                })
                .doOnError(error -> {
                    log.error("네이버 커머스 상품 조회 실패: {}", error.getMessage());
                });
    }
    
    /**
     * 네이버 커머스 상품 목록 조회
     */
    public Mono<Map<String, Object>> getProducts(String accessToken, Map<String, String> queryParams) {
        log.info("네이버 커머스 상품 목록 조회 시작");
        
        String uri = naverApiBaseUrl + "/products";
        
        // 쿼리 파라미터 추가
        if (queryParams != null && !queryParams.isEmpty()) {
            StringBuilder uriBuilder = new StringBuilder(uri + "?");
            queryParams.forEach((key, value) -> 
                uriBuilder.append(key).append("=").append(value).append("&")
            );
            uri = uriBuilder.toString().replaceAll("&$", "");
        }
        
        return webClientBuilder.build()
                .get()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .map(result -> (Map<String, Object>) result)
                .doOnSuccess(response -> {
                    log.info("네이버 커머스 상품 목록 조회 성공");
                })
                .doOnError(error -> {
                    log.error("네이버 커머스 상품 목록 조회 실패: {}", error.getMessage());
                });
    }
    
    /**
     * 네이버 커머스 주문 조회
     */
    public Mono<Map<String, Object>> getOrders(String accessToken, Map<String, String> queryParams) {
        log.info("네이버 커머스 주문 조회 시작");
        
        String uri = naverApiBaseUrl + "/orders";
        
        // 쿼리 파라미터 추가
        if (queryParams != null && !queryParams.isEmpty()) {
            StringBuilder uriBuilder = new StringBuilder(uri + "?");
            queryParams.forEach((key, value) -> 
                uriBuilder.append(key).append("=").append(value).append("&")
            );
            uri = uriBuilder.toString().replaceAll("&$", "");
        }
        
        return webClientBuilder.build()
                .get()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .map(result -> (Map<String, Object>) result)
                .doOnSuccess(response -> {
                    log.info("네이버 커머스 주문 조회 성공");
                })
                .doOnError(error -> {
                    log.error("네이버 커머스 주문 조회 실패: {}", error.getMessage());
                });
    }
    
    /**
     * 네이버 커머스 문의 조회
     */
    public Mono<Map<String, Object>> getInquiries(String accessToken, Map<String, String> queryParams) {
        log.info("네이버 커머스 문의 조회 시작");
        
        String uri = naverApiBaseUrl + "/inquiries";
        
        // 쿼리 파라미터 추가
        if (queryParams != null && !queryParams.isEmpty()) {
            StringBuilder uriBuilder = new StringBuilder(uri + "?");
            queryParams.forEach((key, value) -> 
                uriBuilder.append(key).append("=").append(value).append("&")
            );
            uri = uriBuilder.toString().replaceAll("&$", "");
        }
        
        return webClientBuilder.build()
                .get()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .map(result -> (Map<String, Object>) result)
                .doOnSuccess(response -> {
                    log.info("네이버 커머스 문의 조회 성공");
                })
                .doOnError(error -> {
                    log.error("네이버 커머스 문의 조회 실패: {}", error.getMessage());
                });
    }
    
    /**
     * 네이버 커머스 정산 조회
     */
    public Mono<Map<String, Object>> getSettlements(String accessToken, Map<String, String> queryParams) {
        log.info("네이버 커머스 정산 조회 시작");
        
        String uri = naverApiBaseUrl + "/settlements";
        
        // 쿼리 파라미터 추가
        if (queryParams != null && !queryParams.isEmpty()) {
            StringBuilder uriBuilder = new StringBuilder(uri + "?");
            queryParams.forEach((key, value) -> 
                uriBuilder.append(key).append("=").append(value).append("&")
            );
            uri = uriBuilder.toString().replaceAll("&$", "");
        }
        
        return webClientBuilder.build()
                .get()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .map(result -> (Map<String, Object>) result)
                .doOnSuccess(response -> {
                    log.info("네이버 커머스 정산 조회 성공");
                })
                .doOnError(error -> {
                    log.error("네이버 커머스 정산 조회 실패: {}", error.getMessage());
                });
    }
}
