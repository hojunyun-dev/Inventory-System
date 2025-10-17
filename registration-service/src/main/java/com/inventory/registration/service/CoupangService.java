package com.inventory.registration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoupangService {

    private final WebClient.Builder webClientBuilder;

    @Value("${platforms.coupang.api-base-url}")
    private String coupangApiBaseUrl;
    @Value("${oauth.coupang.api-key:test-api-key}")
    private String apiKey;
    @Value("${platforms.coupang.endpoints.products}")
    private String productsEndpoint;
    @Value("${platforms.coupang.endpoints.orders}")
    private String ordersEndpoint;
    @Value("${platforms.coupang.endpoints.settlements}")
    private String settlementsEndpoint;

    /**
     * 쿠팡 상품 등록
     */
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> registerProduct(Map<String, Object> productData) {
        return webClientBuilder.build()
                .post()
                .uri(coupangApiBaseUrl + productsEndpoint)
                .header(HttpHeaders.AUTHORIZATION, apiKey)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(productData)
                .retrieve()
                .bodyToMono(Map.class)
                .map(result -> (Map<String, Object>) result)
                .doOnSuccess(response -> {
                    log.info("쿠팡 상품 등록 성공: {}", response);
                })
                .doOnError(error -> {
                    log.error("쿠팡 상품 등록 실패: {}", error.getMessage());
                });
    }

    /**
     * 쿠팡 상품 수정
     */
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> updateProduct(String sellerProductId, Map<String, Object> productData) {
        return webClientBuilder.build()
                .put()
                .uri(coupangApiBaseUrl + productsEndpoint + "/" + sellerProductId)
                .header(HttpHeaders.AUTHORIZATION, apiKey)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(productData)
                .retrieve()
                .bodyToMono(Map.class)
                .map(result -> (Map<String, Object>) result)
                .doOnSuccess(response -> {
                    log.info("쿠팡 상품 수정 성공: {}", response);
                })
                .doOnError(error -> {
                    log.error("쿠팡 상품 수정 실패: {}", error.getMessage());
                });
    }

    /**
     * 쿠팡 상품 삭제
     */
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> deleteProduct(String sellerProductId) {
        return webClientBuilder.build()
                .delete()
                .uri(coupangApiBaseUrl + productsEndpoint + "/" + sellerProductId)
                .header(HttpHeaders.AUTHORIZATION, apiKey)
                .retrieve()
                .bodyToMono(Map.class)
                .map(result -> (Map<String, Object>) result)
                .doOnSuccess(response -> {
                    log.info("쿠팡 상품 삭제 성공: {}", response);
                })
                .doOnError(error -> {
                    log.error("쿠팡 상품 삭제 실패: {}", error.getMessage());
                });
    }

    /**
     * 쿠팡 상품 조회
     */
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> getProduct(String sellerProductId) {
        return webClientBuilder.build()
                .get()
                .uri(coupangApiBaseUrl + productsEndpoint + "/" + sellerProductId)
                .header(HttpHeaders.AUTHORIZATION, apiKey)
                .retrieve()
                .bodyToMono(Map.class)
                .map(result -> (Map<String, Object>) result)
                .doOnSuccess(response -> {
                    log.info("쿠팡 상품 조회 성공: {}", response);
                })
                .doOnError(error -> {
                    log.error("쿠팡 상품 조회 실패: {}", error.getMessage());
                });
    }

    /**
     * 쿠팡 상품 목록 조회
     */
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> getAllProducts(Map<String, String> queryParams) {
        return webClientBuilder.build()
                .get()
                .uri(uriBuilder -> {
                    uriBuilder.path(coupangApiBaseUrl + productsEndpoint);
                    queryParams.forEach(uriBuilder::queryParam);
                    return uriBuilder.build();
                })
                .header(HttpHeaders.AUTHORIZATION, apiKey)
                .retrieve()
                .bodyToMono(Map.class)
                .map(result -> (Map<String, Object>) result)
                .doOnSuccess(response -> {
                    log.info("쿠팡 상품 목록 조회 성공: {}", response);
                })
                .doOnError(error -> {
                    log.error("쿠팡 상품 목록 조회 실패: {}", error.getMessage());
                });
    }

    /**
     * 쿠팡 발주서 목록 조회
     */
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> getOrders(Map<String, String> queryParams) {
        return webClientBuilder.build()
                .get()
                .uri(uriBuilder -> {
                    uriBuilder.path(coupangApiBaseUrl + ordersEndpoint);
                    queryParams.forEach(uriBuilder::queryParam);
                    return uriBuilder.build();
                })
                .header(HttpHeaders.AUTHORIZATION, apiKey)
                .retrieve()
                .bodyToMono(Map.class)
                .map(result -> (Map<String, Object>) result)
                .doOnSuccess(response -> {
                    log.info("쿠팡 발주서 목록 조회 성공: {}", response);
                })
                .doOnError(error -> {
                    log.error("쿠팡 발주서 목록 조회 실패: {}", error.getMessage());
                });
    }

    /**
     * 쿠팡 발주서 단건 조회
     */
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> getOrder(String shipmentBoxId) {
        return webClientBuilder.build()
                .get()
                .uri(coupangApiBaseUrl + ordersEndpoint + "/" + shipmentBoxId)
                .header(HttpHeaders.AUTHORIZATION, apiKey)
                .retrieve()
                .bodyToMono(Map.class)
                .map(result -> (Map<String, Object>) result)
                .doOnSuccess(response -> {
                    log.info("쿠팡 발주서 단건 조회 성공: {}", response);
                })
                .doOnError(error -> {
                    log.error("쿠팡 발주서 단건 조회 실패: {}", error.getMessage());
                });
    }

    /**
     * 쿠팡 배송 상태 변경
     */
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> updateShippingStatus(String shipmentBoxId, Map<String, Object> statusData) {
        return webClientBuilder.build()
                .put()
                .uri(coupangApiBaseUrl + ordersEndpoint + "/" + shipmentBoxId + "/acknowledge")
                .header(HttpHeaders.AUTHORIZATION, apiKey)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(statusData)
                .retrieve()
                .bodyToMono(Map.class)
                .map(result -> (Map<String, Object>) result)
                .doOnSuccess(response -> {
                    log.info("쿠팡 배송 상태 변경 성공: {}", response);
                })
                .doOnError(error -> {
                    log.error("쿠팡 배송 상태 변경 실패: {}", error.getMessage());
                });
    }

    /**
     * 쿠팡 매출 내역 조회
     */
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> getSalesHistory(Map<String, String> queryParams) {
        return webClientBuilder.build()
                .get()
                .uri(uriBuilder -> {
                    uriBuilder.path(coupangApiBaseUrl + settlementsEndpoint + "/sales-history");
                    queryParams.forEach(uriBuilder::queryParam);
                    return uriBuilder.build();
                })
                .header(HttpHeaders.AUTHORIZATION, apiKey)
                .retrieve()
                .bodyToMono(Map.class)
                .map(result -> (Map<String, Object>) result)
                .doOnSuccess(response -> {
                    log.info("쿠팡 매출 내역 조회 성공: {}", response);
                })
                .doOnError(error -> {
                    log.error("쿠팡 매출 내역 조회 실패: {}", error.getMessage());
                });
    }

    /**
     * 쿠팡 지급 내역 조회
     */
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> getPaymentHistory(Map<String, String> queryParams) {
        return webClientBuilder.build()
                .get()
                .uri(uriBuilder -> {
                    uriBuilder.path(coupangApiBaseUrl + settlementsEndpoint + "/payment-history");
                    queryParams.forEach(uriBuilder::queryParam);
                    return uriBuilder.build();
                })
                .header(HttpHeaders.AUTHORIZATION, apiKey)
                .retrieve()
                .bodyToMono(Map.class)
                .map(result -> (Map<String, Object>) result)
                .doOnSuccess(response -> {
                    log.info("쿠팡 지급 내역 조회 성공: {}", response);
                })
                .doOnError(error -> {
                    log.error("쿠팡 지급 내역 조회 실패: {}", error.getMessage());
                });
    }
}
