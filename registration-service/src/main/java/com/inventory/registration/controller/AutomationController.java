package com.inventory.registration.controller;

import com.inventory.registration.model.AutomationResult;
import com.inventory.registration.model.ProductData;
import com.inventory.registration.service.AutomationOrchestratorService;
import com.inventory.registration.service.BunjangRegistrationService;
import com.inventory.registration.dto.ProductRegistrationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/automation")
@RequiredArgsConstructor
@Slf4j
public class AutomationController {
    
    private final AutomationOrchestratorService automationOrchestratorService;
    private final BunjangRegistrationService bunjangRegistrationService;
    
    /**
     * 모든 플랫폼에 상품 등록
     */
    @PostMapping("/register")
    public CompletableFuture<ResponseEntity<List<AutomationResult>>> registerProductToAllPlatforms(
            @RequestBody Map<String, Object> request) {
        
        log.info("Received request to register product to all platforms");
        
        try {
            // 요청 데이터 파싱
            ProductData productData = parseProductData(request);
            String username = (String) request.get("username");
            String password = (String) request.get("password");
            
            if (username == null || password == null) {
                return CompletableFuture.completedFuture(
                        ResponseEntity.badRequest().build());
            }
            
            // 모든 플랫폼에 등록
            CompletableFuture<List<AutomationResult>> future = 
                    automationOrchestratorService.registerProductToAllPlatforms(productData, username, password);
            
            return future.thenApply(results -> {
                log.info("Registration completed. Results: {}", results.size());
                return ResponseEntity.ok(results);
            });
            
        } catch (Exception e) {
            log.error("Failed to process registration request: {}", e.getMessage());
            return CompletableFuture.completedFuture(
                    ResponseEntity.status(500).build());
        }
    }
    
    /**
     * 특정 플랫폼에 상품 등록
     */
    @PostMapping("/register/{platform}")
    public ResponseEntity<AutomationResult> registerToPlatform(
            @PathVariable String platform,
            @RequestBody Map<String, Object> request) {
        
        log.info("Received request to register product to platform: {}", platform);
        
        try {
            // 요청 데이터 파싱
            ProductData productData = parseProductData(request);
            String username = (String) request.get("username");
            String password = (String) request.get("password");
            
            if (username == null || password == null) {
                return ResponseEntity.badRequest().build();
            }
            
            // 특정 플랫폼에 등록
            AutomationResult result = automationOrchestratorService.registerProductToPlatform(
                    platform, productData, username, password);
            
            log.info("Registration completed for platform {}: {}", platform, result.isSuccess());
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Failed to process registration request for platform {}: {}", platform, e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * 지원되는 플랫폼 목록 조회
     */
    @GetMapping("/platforms")
    public ResponseEntity<List<String>> getSupportedPlatforms() {
        List<String> platforms = automationOrchestratorService.getSupportedPlatforms();
        return ResponseEntity.ok(platforms);
    }
    
    /**
     * 플랫폼 상태 확인
     */
    @GetMapping("/status")
    public ResponseEntity<List<String>> getPlatformStatus() {
        List<String> status = automationOrchestratorService.getPlatformStatus();
        return ResponseEntity.ok(status);
    }
    
    /**
     * 테스트용 엔드포인트 - 상품 데이터 생성
     */
    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> testRegistration(@RequestBody Map<String, Object> request) {
        log.info("Test registration request received");
        
        try {
            ProductData productData = parseProductData(request);
            
            Map<String, Object> response = Map.of(
                    "message", "Product data parsed successfully",
                    "productName", productData.getName(),
                    "price", productData.getPrice(),
                    "platform", productData.getPlatform()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Test registration failed: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 번개장터 전용 상품 등록 엔드포인트
     */
    @PostMapping("/platform/bunjang/register")
    public ResponseEntity<Map<String, Object>> registerToBunjang(@RequestBody Map<String, Object> request) {
        log.info("Received request to register product to Bunjang");
        
        try {
            // productId는 Integer로 전달될 수 있으므로 String으로 변환
            String productId = String.valueOf(request.get("productId"));
            String productName = (String) request.get("productName");
            String description = (String) request.get("description");
            // price와 quantity도 Integer로 전달될 수 있으므로 String으로 변환
            String price = String.valueOf(request.get("price"));
            String quantity = String.valueOf(request.get("quantity"));
            String category = (String) request.get("category");
            
            log.info("Product details - Name: {}, Price: {}, Category: {}", productName, price, category);
            
            // ProductRegistrationRequest 객체 생성
            ProductRegistrationRequest productRequest = new ProductRegistrationRequest();
            productRequest.setPlatform("bunjang");
            productRequest.setProductId(productId);
            productRequest.setProductName(productName);
            productRequest.setProductDescription(description);
            productRequest.setPrice(Double.parseDouble(price));
            productRequest.setStock(Integer.parseInt(quantity));
            productRequest.setCategory(category);
            
            // 1. 로그인 상태 확인 (토큰 기반)
            Map<String, Object> loginStatus = bunjangRegistrationService.checkLoginStatusLight();
            boolean isLoggedIn = (Boolean) loginStatus.get("loggedIn");
            String loginMessage = (String) loginStatus.get("message");
            
            log.info("Login status check - loggedIn: {}, message: {}", isLoggedIn, loginMessage);
            
            // 2. 토큰이 유효하면 바로 상품 등록 API 호출
            if (isLoggedIn && (loginMessage != null && loginMessage.contains("토큰"))) {
                log.info("✅ 토큰 기반 로그인 상태 확인됨. API로 상품 등록을 진행합니다.");
                
                try {
                    com.inventory.registration.entity.ProductRegistration result = 
                        bunjangRegistrationService.registerProduct(productRequest);
                    
                    return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "토큰 기반 API로 상품 등록이 완료되었습니다.",
                        "registrationId", result.getPlatformProductId() != null ? result.getPlatformProductId() : "unknown",
                        "platformUrl", result.getPlatformUrl() != null ? result.getPlatformUrl() : "unknown"
                    ));
                    
                } catch (Exception e) {
                    log.error("❌ 토큰 기반 상품 등록 실패: {}", e.getMessage());
                    // API 등록 실패 시 브라우저 로그인 플로우로 전환
                    log.info("브라우저 로그인 플로우로 전환합니다.");
                    // 아래 openForManualLogin 호출로 계속 진행
                }
            }
            
            // 3. 토큰이 없거나 만료되었거나 API 등록 실패 시 브라우저 로그인 플로우
            log.info("🔐 브라우저 로그인 플로우를 시작합니다.");
            Map<String, Object> result = bunjangRegistrationService.openForManualLogin(productRequest);
            
            log.info("Bunjang registration result: {}", result);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Failed to register product to Bunjang: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "번개장터 상품 등록 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 번개장터 수동 로그인용 브라우저 창 열기 (상품 정보 포함)
     */
    @PostMapping("/platform/bunjang/session/open-with-product")
    public ResponseEntity<Map<String, Object>> openBunjangSessionWithProduct(@RequestBody Map<String, Object> request) {
        log.info("Received request to open Bunjang session with product: {}", request);
        
        try {
            // productId는 Integer로 전달될 수 있으므로 String으로 변환
            String productId = String.valueOf(request.get("productId"));
            String productName = (String) request.get("productName");
            String description = (String) request.get("description");
            // price와 quantity도 Integer로 전달될 수 있으므로 String으로 변환
            String price = String.valueOf(request.get("price"));
            String quantity = String.valueOf(request.get("quantity"));
            String category = (String) request.get("category");
            
            log.info("Product details - Name: {}, Price: {}, Category: {}", productName, price, category);
            
            // ProductRegistrationRequest 객체 생성
            ProductRegistrationRequest productRequest = new ProductRegistrationRequest();
            productRequest.setPlatform("bunjang");
            productRequest.setProductId(productId);
            productRequest.setProductName(productName);
            productRequest.setProductDescription(description);
            productRequest.setPrice(Double.parseDouble(price));
            productRequest.setStock(Integer.parseInt(quantity));
            productRequest.setCategory(category);
            
            // 번개장터 로그인 창 오픈
            Map<String, Object> result = bunjangRegistrationService.openForManualLogin(productRequest);
            
            log.info("Bunjang session open result: {}", result);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Failed to open Bunjang session: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "번개장터 로그인 창 오픈 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 번개장터 수동 로그인용 브라우저 창 열기
     */
    @PostMapping("/platform/bunjang/session/open")
    public ResponseEntity<Map<String, Object>> openBunjangSession() {
        log.info("Received request to open Bunjang session");
        
        try {
            Map<String, Object> result = bunjangRegistrationService.openForManualLogin(null);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to open Bunjang session: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "번개장터 로그인 창 오픈 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 요청 데이터를 ProductData로 변환
     */
    private ProductData parseProductData(Map<String, Object> request) {
        return ProductData.builder()
                .name((String) request.get("name"))
                .description((String) request.get("description"))
                .price(request.get("price") != null ? 
                        Double.valueOf(request.get("price").toString()) : null)
                .category((String) request.get("category"))
                .condition((String) request.get("condition"))
                .location((String) request.get("location"))
                .platform((String) request.get("platform"))
                .build();
    }
}
