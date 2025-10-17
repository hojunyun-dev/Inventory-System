package com.inventory.registration.controller;

import com.inventory.registration.service.CoupangService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/coupang")
@RequiredArgsConstructor
@Slf4j
public class CoupangController {

    private final CoupangService coupangService;

    @PostMapping("/products")
    public Mono<ResponseEntity<Map<String, Object>>> registerProduct(@RequestBody Map<String, Object> productData) {
        log.info("쿠팡 상품 등록 요청: {}", productData);
        return coupangService.registerProduct(productData)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.badRequest().build())
                .onErrorResume(e -> {
                    log.error("쿠팡 상품 등록 중 오류 발생: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(500).body(Map.of("error", "INTERNAL_ERROR", "message", "An unexpected error occurred. Please try again later.")));
                });
    }

    @PutMapping("/products/{sellerProductId}")
    public Mono<ResponseEntity<Map<String, Object>>> updateProduct(@PathVariable String sellerProductId, @RequestBody Map<String, Object> productData) {
        log.info("쿠팡 상품 수정 요청: sellerProductId={}, data={}", sellerProductId, productData);
        return coupangService.updateProduct(sellerProductId, productData)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(e -> {
                    log.error("쿠팡 상품 수정 중 오류 발생: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(500).body(Map.of("error", "INTERNAL_ERROR", "message", "An unexpected error occurred. Please try again later.")));
                });
    }

    @DeleteMapping("/products/{sellerProductId}")
    public Mono<ResponseEntity<Map<String, Object>>> deleteProduct(@PathVariable String sellerProductId) {
        log.info("쿠팡 상품 삭제 요청: sellerProductId={}", sellerProductId);
        return coupangService.deleteProduct(sellerProductId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(e -> {
                    log.error("쿠팡 상품 삭제 중 오류 발생: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(500).body(Map.of("error", "INTERNAL_ERROR", "message", "An unexpected error occurred. Please try again later.")));
                });
    }

    @GetMapping("/products/{sellerProductId}")
    public Mono<ResponseEntity<Map<String, Object>>> getProduct(@PathVariable String sellerProductId) {
        log.info("쿠팡 상품 조회 요청: sellerProductId={}", sellerProductId);
        return coupangService.getProduct(sellerProductId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(e -> {
                    log.error("쿠팡 상품 조회 중 오류 발생: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(500).body(Map.of("error", "INTERNAL_ERROR", "message", "An unexpected error occurred. Please try again later.")));
                });
    }

    @GetMapping("/products")
    public Mono<ResponseEntity<Map<String, Object>>> getAllProducts(@RequestParam Map<String, String> queryParams) {
        log.info("쿠팡 상품 목록 조회 요청: queryParams={}", queryParams);
        return coupangService.getAllProducts(queryParams)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(e -> {
                    log.error("쿠팡 상품 목록 조회 중 오류 발생: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(500).body(Map.of("error", "INTERNAL_ERROR", "message", "An unexpected error occurred. Please try again later.")));
                });
    }

    @GetMapping("/orders")
    public Mono<ResponseEntity<Map<String, Object>>> getOrders(@RequestParam Map<String, String> queryParams) {
        log.info("쿠팡 발주서 목록 조회 요청: queryParams={}", queryParams);
        return coupangService.getOrders(queryParams)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(e -> {
                    log.error("쿠팡 발주서 목록 조회 중 오류 발생: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(500).body(Map.of("error", "INTERNAL_ERROR", "message", "An unexpected error occurred. Please try again later.")));
                });
    }

    @GetMapping("/orders/{shipmentBoxId}")
    public Mono<ResponseEntity<Map<String, Object>>> getOrder(@PathVariable String shipmentBoxId) {
        log.info("쿠팡 발주서 단건 조회 요청: shipmentBoxId={}", shipmentBoxId);
        return coupangService.getOrder(shipmentBoxId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(e -> {
                    log.error("쿠팡 발주서 단건 조회 중 오류 발생: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(500).body(Map.of("error", "INTERNAL_ERROR", "message", "An unexpected error occurred. Please try again later.")));
                });
    }

    @PutMapping("/orders/{shipmentBoxId}/acknowledge")
    public Mono<ResponseEntity<Map<String, Object>>> updateShippingStatus(@PathVariable String shipmentBoxId, @RequestBody Map<String, Object> statusData) {
        log.info("쿠팡 배송 상태 변경 요청: shipmentBoxId={}, data={}", shipmentBoxId, statusData);
        return coupangService.updateShippingStatus(shipmentBoxId, statusData)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(e -> {
                    log.error("쿠팡 배송 상태 변경 중 오류 발생: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(500).body(Map.of("error", "INTERNAL_ERROR", "message", "An unexpected error occurred. Please try again later.")));
                });
    }

    @GetMapping("/settlements/sales-history")
    public Mono<ResponseEntity<Map<String, Object>>> getSalesHistory(@RequestParam Map<String, String> queryParams) {
        log.info("쿠팡 매출 내역 조회 요청: queryParams={}", queryParams);
        return coupangService.getSalesHistory(queryParams)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(e -> {
                    log.error("쿠팡 매출 내역 조회 중 오류 발생: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(500).body(Map.of("error", "INTERNAL_ERROR", "message", "An unexpected error occurred. Please try again later.")));
                });
    }

    @GetMapping("/settlements/payment-history")
    public Mono<ResponseEntity<Map<String, Object>>> getPaymentHistory(@RequestParam Map<String, String> queryParams) {
        log.info("쿠팡 지급 내역 조회 요청: queryParams={}", queryParams);
        return coupangService.getPaymentHistory(queryParams)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(e -> {
                    log.error("쿠팡 지급 내역 조회 중 오류 발생: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(500).body(Map.of("error", "INTERNAL_ERROR", "message", "An unexpected error occurred. Please try again later.")));
                });
    }
}
