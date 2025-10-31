package com.inventory.controller;

import com.inventory.service.DataSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 데이터 동기화 API 컨트롤러
 */
@RestController
@RequestMapping("/api/sync")
public class DataSyncController {

    @Autowired
    private DataSyncService dataSyncService;

    /**
     * 특정 플랫폼의 토큰 데이터 동기화
     */
    @GetMapping("/tokens/{platform}")
    public Mono<ResponseEntity<Map<String, Object>>> syncTokenData(@PathVariable String platform) {
        return dataSyncService.syncTokenData(platform)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.internalServerError().build());
    }

    /**
     * 특정 상품의 등록 데이터 동기화
     */
    @GetMapping("/registrations/{productId}")
    public Mono<ResponseEntity<Map<String, Object>>> syncRegistrationData(@PathVariable String productId) {
        return dataSyncService.syncRegistrationData(productId)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.internalServerError().build());
    }

    /**
     * 전체 서비스 간 데이터 동기화
     */
    @PostMapping("/all")
    public Mono<ResponseEntity<String>> syncAllServices() {
        return dataSyncService.syncAllServices()
                .then(Mono.just(ResponseEntity.ok("전체 서비스 동기화가 완료되었습니다.")))
                .onErrorReturn(ResponseEntity.internalServerError().body("서비스 동기화 중 오류가 발생했습니다."));
    }

    /**
     * 동기화 상태 확인
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> getSyncStatus() {
        return ResponseEntity.ok(Map.of(
                "status", "active",
                "lastSync", java.time.LocalDateTime.now().toString(),
                "message", "데이터 동기화 서비스가 정상적으로 실행 중입니다."
        ));
    }
}
