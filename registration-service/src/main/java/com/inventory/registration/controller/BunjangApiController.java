package com.inventory.registration.controller;

import com.example.common.dto.ProductRegisterRequest;
import com.example.common.dto.TokenBundle;
import com.inventory.registration.service.bunjang.BunjangApiRegistrationService;
import com.inventory.registration.service.bunjang.CdpTokenCaptureService;
import com.inventory.registration.service.bunjang.TokenBundleService;
import com.inventory.registration.service.bunjang.IntegratedBunjangService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 번개장터 API 기반 상품 등록 컨트롤러
 * - 토큰 캡처 및 저장
 * - API 기반 상품 등록
 * - 토큰 상태 관리
 */
@RestController
@RequestMapping("/api/bunjang")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class BunjangApiController {

    private final CdpTokenCaptureService cdpTokenCaptureService;
    private final TokenBundleService tokenBundleService;
    private final BunjangApiRegistrationService apiRegistrationService;
    private final IntegratedBunjangService integratedService;

    /**
     * 로그인 및 토큰 캡처 (통합 플로우)
     */
    @PostMapping("/capture-token")
    public ResponseEntity<Map<String, Object>> captureToken() {
        try {
            log.info("🔍 Starting integrated login and token capture process...");
            
            Map<String, Object> result = integratedService.loginAndCaptureToken();
            
            if ((Boolean) result.get("success")) {
                log.info("✅ Integrated login and token capture completed successfully");
                return ResponseEntity.ok(result);
            } else {
                log.error("❌ Integrated login and token capture failed");
                return ResponseEntity.badRequest().body(result);
            }
            
        } catch (Exception e) {
            log.error("❌ Integrated login and token capture failed: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Integrated login and token capture failed: " + e.getMessage()
            ));
        }
    }

    /**
     * 상품 등록 (통합 플로우)
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerProduct(@RequestBody ProductRegisterRequest request) {
        try {
            log.info("📦 Starting integrated product registration: {}", request.name);
            
            Map<String, Object> result = integratedService.registerProduct(request);
            
            if ((Boolean) result.get("success")) {
                log.info("✅ Integrated product registration completed successfully");
                return ResponseEntity.ok(result);
            } else {
                log.error("❌ Integrated product registration failed");
                return ResponseEntity.badRequest().body(result);
            }
            
        } catch (Exception e) {
            log.error("❌ Integrated product registration failed: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Integrated product registration failed: " + e.getMessage()
            ));
        }
    }

    /**
     * 토큰 상태 조회
     */
    @GetMapping("/token-status")
    public ResponseEntity<Map<String, Object>> getTokenStatus() {
        try {
            Map<String, Object> status = apiRegistrationService.getTokenStatus();
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("❌ Failed to get token status: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Failed to get token status: " + e.getMessage()
            ));
        }
    }
    

    /**
     * 모든 토큰 상태 조회
     */
    @GetMapping("/tokens/status")
    public ResponseEntity<Map<String, Object>> getAllTokenStatus() {
        try {
            Map<String, Object> status = tokenBundleService.getAllTokenStatus();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "tokens", status
            ));
        } catch (Exception e) {
            log.error("❌ Failed to get all token status: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Failed to get token status: " + e.getMessage()
            ));
        }
    }

    /**
     * 토큰 삭제
     */
    @DeleteMapping("/token")
    public ResponseEntity<Map<String, Object>> deleteToken() {
        try {
            tokenBundleService.deleteTokenBundle("BUNJANG");
            log.info("✅ Token deleted successfully");
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Token deleted successfully"
            ));
        } catch (Exception e) {
            log.error("❌ Failed to delete token: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Failed to delete token: " + e.getMessage()
            ));
        }
    }

    /**
     * 토큰 유효성 검사
     */
    @GetMapping("/token/valid")
    public ResponseEntity<Map<String, Object>> isTokenValid() {
        try {
            boolean isValid = apiRegistrationService.isTokenValid();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "isValid", isValid,
                "message", isValid ? "Token is valid" : "Token is invalid or expired"
            ));
        } catch (Exception e) {
            log.error("❌ Failed to check token validity: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Failed to check token validity: " + e.getMessage()
            ));
        }
    }
}
