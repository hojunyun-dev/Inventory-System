package com.inventory.registration.controller;

import com.inventory.registration.service.Cafe24Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/cafe24")
@RequiredArgsConstructor
@Slf4j
public class Cafe24Controller {

    private final Cafe24Service cafe24Service;

    @PostMapping("/products")
    public Mono<ResponseEntity<Map<String, Object>>> registerProduct(@RequestBody Map<String, Object> productData) {
        log.info("카페24 상품 등록 요청: {}", productData);
        return cafe24Service.registerProduct(productData)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.badRequest().build())
                .onErrorResume(e -> {
                    log.error("카페24 상품 등록 중 오류 발생: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(500).body(Map.of("error", "INTERNAL_ERROR", "message", "An unexpected error occurred. Please try again later.")));
                });
    }

    @PutMapping("/products/{productId}")
    public Mono<ResponseEntity<Map<String, Object>>> updateProduct(@PathVariable String productId, @RequestBody Map<String, Object> productData) {
        log.info("카페24 상품 수정 요청: productId={}, data={}", productId, productData);
        return cafe24Service.updateProduct(productId, productData)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(e -> {
                    log.error("카페24 상품 수정 중 오류 발생: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(500).body(Map.of("error", "INTERNAL_ERROR", "message", "An unexpected error occurred. Please try again later.")));
                });
    }

    @DeleteMapping("/products/{productId}")
    public Mono<ResponseEntity<Map<String, Object>>> deleteProduct(@PathVariable String productId) {
        log.info("카페24 상품 삭제 요청: productId={}", productId);
        return cafe24Service.deleteProduct(productId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(e -> {
                    log.error("카페24 상품 삭제 중 오류 발생: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(500).body(Map.of("error", "INTERNAL_ERROR", "message", "An unexpected error occurred. Please try again later.")));
                });
    }

    @GetMapping("/products/{productId}")
    public Mono<ResponseEntity<Map<String, Object>>> getProduct(@PathVariable String productId) {
        log.info("카페24 상품 조회 요청: productId={}", productId);
        return cafe24Service.getProduct(productId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(e -> {
                    log.error("카페24 상품 조회 중 오류 발생: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(500).body(Map.of("error", "INTERNAL_ERROR", "message", "An unexpected error occurred. Please try again later.")));
                });
    }

    @GetMapping("/products")
    public Mono<ResponseEntity<Map<String, Object>>> getAllProducts(@RequestParam Map<String, String> queryParams) {
        log.info("카페24 상품 목록 조회 요청: queryParams={}", queryParams);
        return cafe24Service.getAllProducts(queryParams)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(e -> {
                    log.error("카페24 상품 목록 조회 중 오류 발생: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(500).body(Map.of("error", "INTERNAL_ERROR", "message", "An unexpected error occurred. Please try again later.")));
                });
    }

    @GetMapping("/orders")
    public Mono<ResponseEntity<Map<String, Object>>> getOrders(@RequestParam Map<String, String> queryParams) {
        log.info("카페24 주문 조회 요청: queryParams={}", queryParams);
        return cafe24Service.getOrders(queryParams)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(e -> {
                    log.error("카페24 주문 조회 중 오류 발생: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(500).body(Map.of("error", "INTERNAL_ERROR", "message", "An unexpected error occurred. Please try again later.")));
                });
    }

    @GetMapping("/customers")
    public Mono<ResponseEntity<Map<String, Object>>> getCustomers(@RequestParam Map<String, String> queryParams) {
        log.info("카페24 고객 조회 요청: queryParams={}", queryParams);
        return cafe24Service.getCustomers(queryParams)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(e -> {
                    log.error("카페24 고객 조회 중 오류 발생: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(500).body(Map.of("error", "INTERNAL_ERROR", "message", "An unexpected error occurred. Please try again later.")));
                });
    }

    @GetMapping("/categories")
    public Mono<ResponseEntity<Map<String, Object>>> getCategories(@RequestParam Map<String, String> queryParams) {
        log.info("카페24 카테고리 조회 요청: queryParams={}", queryParams);
        return cafe24Service.getCategories(queryParams)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(e -> {
                    log.error("카페24 카테고리 조회 중 오류 발생: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(500).body(Map.of("error", "INTERNAL_ERROR", "message", "An unexpected error occurred. Please try again later.")));
                });
    }

    @GetMapping("/boards")
    public Mono<ResponseEntity<Map<String, Object>>> getBoards(@RequestParam Map<String, String> queryParams) {
        log.info("카페24 게시판 조회 요청: queryParams={}", queryParams);
        return cafe24Service.getBoards(queryParams)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(e -> {
                    log.error("카페24 게시판 조회 중 오류 발생: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(500).body(Map.of("error", "INTERNAL_ERROR", "message", "An unexpected error occurred. Please try again later.")));
                });
    }
}
