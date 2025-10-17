package com.inventory.registration.controller;

import com.inventory.registration.service.NaverCommerceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/naver-commerce")
@RequiredArgsConstructor
@Slf4j
public class NaverCommerceController {
    
    private final NaverCommerceService naverCommerceService;
    
    /**
     * 네이버 커머스 상품 등록
     */
    @PostMapping("/products")
    public Mono<ResponseEntity<Map<String, Object>>> registerProduct(
            @RequestBody Map<String, Object> productData,
            @RequestHeader("Authorization") String authorization) {
        
        String accessToken = authorization.replace("Bearer ", "");
        log.info("네이버 커머스 상품 등록 요청: {}", productData.get("name"));
        
        return naverCommerceService.registerProduct(productData, accessToken)
                .map(response -> ResponseEntity.ok(response))
                .onErrorReturn(ResponseEntity.badRequest().body(Map.of("error", "상품 등록 실패")));
    }
    
    /**
     * 네이버 커머스 상품 수정
     */
    @PutMapping("/products/{productId}")
    public Mono<ResponseEntity<Map<String, Object>>> updateProduct(
            @PathVariable String productId,
            @RequestBody Map<String, Object> productData,
            @RequestHeader("Authorization") String authorization) {
        
        String accessToken = authorization.replace("Bearer ", "");
        log.info("네이버 커머스 상품 수정 요청: {}", productId);
        
        return naverCommerceService.updateProduct(productId, productData, accessToken)
                .map(response -> ResponseEntity.ok(response))
                .onErrorReturn(ResponseEntity.badRequest().body(Map.of("error", "상품 수정 실패")));
    }
    
    /**
     * 네이버 커머스 상품 삭제
     */
    @DeleteMapping("/products/{productId}")
    public Mono<ResponseEntity<Map<String, Object>>> deleteProduct(
            @PathVariable String productId,
            @RequestHeader("Authorization") String authorization) {
        
        String accessToken = authorization.replace("Bearer ", "");
        log.info("네이버 커머스 상품 삭제 요청: {}", productId);
        
        return naverCommerceService.deleteProduct(productId, accessToken)
                .map(response -> ResponseEntity.ok(response))
                .onErrorReturn(ResponseEntity.badRequest().body(Map.of("error", "상품 삭제 실패")));
    }
    
    /**
     * 네이버 커머스 상품 조회
     */
    @GetMapping("/products/{productId}")
    public Mono<ResponseEntity<Map<String, Object>>> getProduct(
            @PathVariable String productId,
            @RequestHeader("Authorization") String authorization) {
        
        String accessToken = authorization.replace("Bearer ", "");
        log.info("네이버 커머스 상품 조회 요청: {}", productId);
        
        return naverCommerceService.getProduct(productId, accessToken)
                .map(response -> ResponseEntity.ok(response))
                .onErrorReturn(ResponseEntity.badRequest().body(Map.of("error", "상품 조회 실패")));
    }
    
    /**
     * 네이버 커머스 상품 목록 조회
     */
    @GetMapping("/products")
    public Mono<ResponseEntity<Map<String, Object>>> getProducts(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(required = false) Map<String, String> queryParams) {
        
        String accessToken = authorization.replace("Bearer ", "");
        log.info("네이버 커머스 상품 목록 조회 요청");
        
        return naverCommerceService.getProducts(accessToken, queryParams)
                .map(response -> ResponseEntity.ok(response))
                .onErrorReturn(ResponseEntity.badRequest().body(Map.of("error", "상품 목록 조회 실패")));
    }
    
    /**
     * 네이버 커머스 주문 조회
     */
    @GetMapping("/orders")
    public Mono<ResponseEntity<Map<String, Object>>> getOrders(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(required = false) Map<String, String> queryParams) {
        
        String accessToken = authorization.replace("Bearer ", "");
        log.info("네이버 커머스 주문 조회 요청");
        
        return naverCommerceService.getOrders(accessToken, queryParams)
                .map(response -> ResponseEntity.ok(response))
                .onErrorReturn(ResponseEntity.badRequest().body(Map.of("error", "주문 조회 실패")));
    }
    
    /**
     * 네이버 커머스 문의 조회
     */
    @GetMapping("/inquiries")
    public Mono<ResponseEntity<Map<String, Object>>> getInquiries(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(required = false) Map<String, String> queryParams) {
        
        String accessToken = authorization.replace("Bearer ", "");
        log.info("네이버 커머스 문의 조회 요청");
        
        return naverCommerceService.getInquiries(accessToken, queryParams)
                .map(response -> ResponseEntity.ok(response))
                .onErrorReturn(ResponseEntity.badRequest().body(Map.of("error", "문의 조회 실패")));
    }
    
    /**
     * 네이버 커머스 정산 조회
     */
    @GetMapping("/settlements")
    public Mono<ResponseEntity<Map<String, Object>>> getSettlements(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(required = false) Map<String, String> queryParams) {
        
        String accessToken = authorization.replace("Bearer ", "");
        log.info("네이버 커머스 정산 조회 요청");
        
        return naverCommerceService.getSettlements(accessToken, queryParams)
                .map(response -> ResponseEntity.ok(response))
                .onErrorReturn(ResponseEntity.badRequest().body(Map.of("error", "정산 조회 실패")));
    }
}
