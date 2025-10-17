package com.inventory.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 서비스 간 데이터 동기화를 담당하는 서비스
 */
@Service
public class DataSyncService {

    @Autowired
    private WebClient.Builder webClientBuilder;

    private final String TOKEN_SERVICE_URL = "http://localhost:8081";
    private final String REGISTRATION_SERVICE_URL = "http://localhost:8082";

    /**
     * 토큰 관리 서비스와 데이터 동기화
     */
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> syncTokenData(String platform) {
        return webClientBuilder.build()
                .get()
                .uri(TOKEN_SERVICE_URL + "/api/tokens/{platform}", platform)
                .retrieve()
                .bodyToMono(Map.class)
                .map(result -> (Map<String, Object>) result)
                .doOnSuccess(result -> {
                    System.out.println("토큰 데이터 동기화 완료: " + platform + " - " + LocalDateTime.now());
                })
                .doOnError(error -> {
                    System.err.println("토큰 데이터 동기화 실패: " + platform + " - " + error.getMessage());
                });
    }

    /**
     * 등록 서비스와 데이터 동기화
     */
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> syncRegistrationData(String productId) {
        return webClientBuilder.build()
                .get()
                .uri(REGISTRATION_SERVICE_URL + "/api/registrations/{productId}", productId)
                .retrieve()
                .bodyToMono(Map.class)
                .map(result -> (Map<String, Object>) result)
                .doOnSuccess(result -> {
                    System.out.println("등록 데이터 동기화 완료: " + productId + " - " + LocalDateTime.now());
                })
                .doOnError(error -> {
                    System.err.println("등록 데이터 동기화 실패: " + productId + " - " + error.getMessage());
                });
    }

    /**
     * 전체 서비스 간 데이터 동기화
     */
    public Mono<Void> syncAllServices() {
        return Mono.when(
                syncTokenData("naver"),
                syncTokenData("coupang"),
                syncTokenData("cafe24")
        ).then()
         .doOnSuccess(result -> {
             System.out.println("전체 서비스 동기화 완료: " + LocalDateTime.now());
         })
         .doOnError(error -> {
             System.err.println("전체 서비스 동기화 실패: " + error.getMessage());
         });
    }
}
