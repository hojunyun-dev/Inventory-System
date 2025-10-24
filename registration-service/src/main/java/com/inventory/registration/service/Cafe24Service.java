package com.inventory.registration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class Cafe24Service {

    private final WebClient.Builder webClientBuilder;

    @Value("${platforms.cafe24.api-base-url}")
    private String cafe24ApiBaseUrl;
    @Value("${oauth.cafe24.mall-id:default-mall-id}")
    private String mallId;
    @Value("${oauth.cafe24.client-id:test-client-id}")
    private String clientId;
    @Value("${oauth.cafe24.client-secret:test-client-secret}")
    private String clientSecret;
    @Value("${platforms.cafe24.endpoints.products}")
    private String productsEndpoint;
    @Value("${platforms.cafe24.endpoints.orders}")
    private String ordersEndpoint;
    @Value("${platforms.cafe24.endpoints.customers}")
    private String customersEndpoint;
    @Value("${platforms.cafe24.endpoints.boards}")
    private String boardsEndpoint;
    @Value("${platforms.cafe24.endpoints.categories}")
    private String categoriesEndpoint;

    /**
     * 카페24 상품 등록
     */
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> registerProduct(Map<String, Object> productData) {
        String accessToken = "Bearer " + clientId; // 임시 토큰
        return webClientBuilder.build()
                .post()
                .uri(cafe24ApiBaseUrl + productsEndpoint)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(productData)
                .retrieve()
                .bodyToMono(Map.class)
                .map(result -> (Map<String, Object>) result)
                .doOnSuccess(response -> {
                    log.info("카페24 상품 등록 성공: {}", response);
                })
                .doOnError(error -> {
                    log.error("카페24 상품 등록 실패: {}", error.getMessage());
                });
    }

    /**
     * 카페24 상품 수정
     */
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> updateProduct(String productId, Map<String, Object> productData) {
        String accessToken = "Bearer " + clientId; // 임시 토큰
        return webClientBuilder.build()
                .put()
                .uri(cafe24ApiBaseUrl + productsEndpoint + "/" + productId)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(productData)
                .retrieve()
                .bodyToMono(Map.class)
                .map(result -> (Map<String, Object>) result)
                .doOnSuccess(response -> {
                    log.info("카페24 상품 수정 성공: {}", response);
                })
                .doOnError(error -> {
                    log.error("카페24 상품 수정 실패: {}", error.getMessage());
                });
    }

    /**
     * 카페24 상품 삭제
     */
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> deleteProduct(String productId) {
        String accessToken = "Bearer " + clientId; // 임시 토큰
        return webClientBuilder.build()
                .delete()
                .uri(cafe24ApiBaseUrl + productsEndpoint + "/" + productId)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .map(result -> (Map<String, Object>) result)
                .doOnSuccess(response -> {
                    log.info("카페24 상품 삭제 성공: {}", response);
                })
                .doOnError(error -> {
                    log.error("카페24 상품 삭제 실패: {}", error.getMessage());
                });
    }

    /**
     * 카페24 상품 조회
     */
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> getProduct(String productId) {
        String accessToken = "Bearer " + clientId; // 임시 토큰
        return webClientBuilder.build()
                .get()
                .uri(cafe24ApiBaseUrl + productsEndpoint + "/" + productId)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .map(result -> (Map<String, Object>) result)
                .doOnSuccess(response -> {
                    log.info("카페24 상품 조회 성공: {}", response);
                })
                .doOnError(error -> {
                    log.error("카페24 상품 조회 실패: {}", error.getMessage());
                });
    }

    /**
     * 카페24 상품 목록 조회
     */
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> getAllProducts(Map<String, String> queryParams) {
        String accessToken = "Bearer " + clientId; // 임시 토큰
        return webClientBuilder.build()
                .get()
                .uri(uriBuilder -> {
                    uriBuilder.path(cafe24ApiBaseUrl + productsEndpoint);
                    queryParams.forEach(uriBuilder::queryParam);
                    return uriBuilder.build();
                })
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .map(result -> (Map<String, Object>) result)
                .doOnSuccess(response -> {
                    log.info("카페24 상품 목록 조회 성공: {}", response);
                })
                .doOnError(error -> {
                    log.error("카페24 상품 목록 조회 실패: {}", error.getMessage());
                });
    }

    /**
     * 카페24 주문 조회
     */
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> getOrders(Map<String, String> queryParams) {
        String accessToken = "Bearer " + clientId; // 임시 토큰
        return webClientBuilder.build()
                .get()
                .uri(uriBuilder -> {
                    uriBuilder.path(cafe24ApiBaseUrl + ordersEndpoint);
                    queryParams.forEach(uriBuilder::queryParam);
                    return uriBuilder.build();
                })
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .map(result -> (Map<String, Object>) result)
                .doOnSuccess(response -> {
                    log.info("카페24 주문 조회 성공: {}", response);
                })
                .doOnError(error -> {
                    log.error("카페24 주문 조회 실패: {}", error.getMessage());
                });
    }

    /**
     * 카페24 고객 조회
     */
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> getCustomers(Map<String, String> queryParams) {
        String accessToken = "Bearer " + clientId; // 임시 토큰
        return webClientBuilder.build()
                .get()
                .uri(uriBuilder -> {
                    uriBuilder.path(cafe24ApiBaseUrl + customersEndpoint);
                    queryParams.forEach(uriBuilder::queryParam);
                    return uriBuilder.build();
                })
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .map(result -> (Map<String, Object>) result)
                .doOnSuccess(response -> {
                    log.info("카페24 고객 조회 성공: {}", response);
                })
                .doOnError(error -> {
                    log.error("카페24 고객 조회 실패: {}", error.getMessage());
                });
    }

    /**
     * 카페24 카테고리 조회
     */
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> getCategories(Map<String, String> queryParams) {
        String accessToken = "Bearer " + clientId; // 임시 토큰
        return webClientBuilder.build()
                .get()
                .uri(uriBuilder -> {
                    uriBuilder.path(cafe24ApiBaseUrl + categoriesEndpoint);
                    queryParams.forEach(uriBuilder::queryParam);
                    return uriBuilder.build();
                })
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .map(result -> (Map<String, Object>) result)
                .doOnSuccess(response -> {
                    log.info("카페24 카테고리 조회 성공: {}", response);
                })
                .doOnError(error -> {
                    log.error("카페24 카테고리 조회 실패: {}", error.getMessage());
                });
    }

    /**
     * 카페24 게시판 조회
     */
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> getBoards(Map<String, String> queryParams) {
        String accessToken = "Bearer " + clientId; // 임시 토큰
        return webClientBuilder.build()
                .get()
                .uri(uriBuilder -> {
                    uriBuilder.path(cafe24ApiBaseUrl + boardsEndpoint);
                    queryParams.forEach(uriBuilder::queryParam);
                    return uriBuilder.build();
                })
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .map(result -> (Map<String, Object>) result)
                .doOnSuccess(response -> {
                    log.info("카페24 게시판 조회 성공: {}", response);
                })
                .doOnError(error -> {
                    log.error("카페24 게시판 조회 실패: {}", error.getMessage());
                });
    }
}
