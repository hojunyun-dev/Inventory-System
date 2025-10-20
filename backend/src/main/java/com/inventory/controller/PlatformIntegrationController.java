package com.inventory.controller;

import com.inventory.dto.PlatformProductRequest;
import com.inventory.dto.PlatformProductResponse;
import com.inventory.service.PlatformIntegrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/platform")
@RequiredArgsConstructor
@Slf4j
public class PlatformIntegrationController {

    private final PlatformIntegrationService platformIntegrationService;

    /**
     * 플랫폼별 상품 등록
     */
    @PostMapping("/{platform}/register")
    public Mono<ResponseEntity<PlatformProductResponse>> registerProduct(
            @PathVariable String platform,
            @Valid @RequestBody PlatformProductRequest request) {
        
        log.info("POST /api/platform/{}/register - product: {}", platform, request.getProductName());
        
        return platformIntegrationService.registerProductToPlatform(platform, request)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("Failed to register product to platform {}: {}", platform, e.getMessage());
                    return Mono.just(ResponseEntity.badRequest().build());
                });
    }

    /**
     * 다중 플랫폼 상품 등록
     */
    @PostMapping("/bulk-register")
    public Mono<ResponseEntity<List<PlatformProductResponse>>> registerToMultiplePlatforms(
            @RequestBody List<PlatformProductRequest> requests) {
        
        log.info("POST /api/platform/bulk-register - {} products", requests.size());
        
        return Mono.just(ResponseEntity.ok().build()); // TODO: 구현 필요
    }

    /**
     * 플랫폼별 등록 상태 조회
     */
    @GetMapping("/{platform}/status")
    public Mono<ResponseEntity<Object>> getRegistrationStatus(@PathVariable String platform) {
        log.info("GET /api/platform/{}/status", platform);
        
        return Mono.just(ResponseEntity.ok().build()); // TODO: 구현 필요
    }

    /**
     * 지원되는 플랫폼 목록 조회
     */
    @GetMapping("/supported")
    public ResponseEntity<List<String>> getSupportedPlatforms() {
        log.info("GET /api/platform/supported");
        
        List<String> supportedPlatforms = List.of(
            "naver", "cafe24", "coupang", "bunjang", "danggeun"
        );
        
        return ResponseEntity.ok(supportedPlatforms);
    }
}

