package com.inventory.registration.controller;

import com.inventory.registration.model.AutomationResult;
import com.inventory.registration.model.ProductData;
import com.inventory.registration.service.NaverSocialAutomationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/automation/naver-social")
@RequiredArgsConstructor
@Slf4j
public class NaverSocialAutomationController {
    
    private final NaverSocialAutomationService naverSocialAutomationService;
    
    /**
     * 네이버 로그인으로 번개장터 상품 등록
     */
    @PostMapping("/bunjang/register")
    public ResponseEntity<AutomationResult> registerProductToBunjang(
            @RequestBody Map<String, Object> request) {
        
        try {
            // 요청 데이터 파싱
            ProductData productData = parseProductData(request);
            String accessToken = (String) request.get("accessToken");
            
            if (accessToken == null || accessToken.isEmpty()) {
                AutomationResult errorResult = AutomationResult.builder()
                    .platform("bunjang")
                    .success(false)
                    .errorMessage("액세스 토큰이 필요합니다")
                    .markAsCompleted()
                    .build();
                return ResponseEntity.badRequest().body(errorResult);
            }
            
            log.info("네이버 로그인으로 번개장터 상품 등록 요청: {}", productData.getName());
            
            AutomationResult result = naverSocialAutomationService
                .registerProductToBunjangWithNaver(productData, accessToken);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("번개장터 상품 등록 요청 처리 실패: {}", e.getMessage());
            AutomationResult errorResult = AutomationResult.builder()
                .platform("bunjang")
                .success(false)
                .errorMessage("요청 처리 실패: " + e.getMessage())
                .markAsCompleted()
                .build();
            return ResponseEntity.badRequest().body(errorResult);
        }
    }
    
    /**
     * 네이버 로그인으로 중고나라 상품 등록
     */
    @PostMapping("/junggonara/register")
    public ResponseEntity<AutomationResult> registerProductToJunggonara(
            @RequestBody Map<String, Object> request) {
        
        try {
            // 요청 데이터 파싱
            ProductData productData = parseProductData(request);
            String accessToken = (String) request.get("accessToken");
            
            if (accessToken == null || accessToken.isEmpty()) {
                AutomationResult errorResult = AutomationResult.builder()
                    .platform("junggonara")
                    .success(false)
                    .errorMessage("액세스 토큰이 필요합니다")
                    .markAsCompleted()
                    .build();
                return ResponseEntity.badRequest().body(errorResult);
            }
            
            log.info("네이버 로그인으로 중고나라 상품 등록 요청: {}", productData.getName());
            
            AutomationResult result = naverSocialAutomationService
                .registerProductToJunggonaraWithNaver(productData, accessToken);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("중고나라 상품 등록 요청 처리 실패: {}", e.getMessage());
            AutomationResult errorResult = AutomationResult.builder()
                .platform("junggonara")
                .success(false)
                .errorMessage("요청 처리 실패: " + e.getMessage())
                .markAsCompleted()
                .build();
            return ResponseEntity.badRequest().body(errorResult);
        }
    }
    
    /**
     * 네이버 로그인으로 모든 플랫폼에 상품 등록
     */
    @PostMapping("/all/register")
    public ResponseEntity<Map<String, Object>> registerProductToAllPlatforms(
            @RequestBody Map<String, Object> request) {
        
        try {
            // 요청 데이터 파싱
            ProductData productData = parseProductData(request);
            String accessToken = (String) request.get("accessToken");
            
            if (accessToken == null || accessToken.isEmpty()) {
                Map<String, Object> errorResponse = new java.util.HashMap<>();
                errorResponse.put("error", "액세스 토큰이 필요합니다");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            log.info("네이버 로그인으로 모든 플랫폼 상품 등록 요청: {}", productData.getName());
            
            // 번개장터 등록
            AutomationResult bunjangResult = naverSocialAutomationService
                .registerProductToBunjangWithNaver(productData, accessToken);
            
            // 중고나라 등록
            AutomationResult junggonaraResult = naverSocialAutomationService
                .registerProductToJunggonaraWithNaver(productData, accessToken);
            
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("bunjang", bunjangResult);
            response.put("junggonara", junggonaraResult);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("모든 플랫폼 상품 등록 요청 처리 실패: {}", e.getMessage());
            Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("error", "요청 처리 실패: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 지원하는 플랫폼 목록 조회
     */
    @GetMapping("/platforms")
    public ResponseEntity<Map<String, Object>> getSupportedPlatforms() {
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("platforms", new String[]{"bunjang", "junggonara"});
        response.put("loginMethod", "네이버 OAuth 2.0");
        response.put("description", "네이버 로그인을 통한 소셜 자동화");
        return ResponseEntity.ok(response);
    }
    
    /**
     * 플랫폼별 상태 조회
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getPlatformStatus() {
        Map<String, Object> response = new java.util.HashMap<>();
        
        Map<String, Object> bunjangStatus = new java.util.HashMap<>();
        bunjangStatus.put("status", "available");
        bunjangStatus.put("loginMethod", "네이버 OAuth");
        bunjangStatus.put("description", "네이버 로그인으로 번개장터 자동화 가능");
        
        Map<String, Object> junggonaraStatus = new java.util.HashMap<>();
        junggonaraStatus.put("status", "available");
        junggonaraStatus.put("loginMethod", "네이버 OAuth");
        junggonaraStatus.put("description", "네이버 로그인으로 중고나라 자동화 가능");
        
        response.put("bunjang", bunjangStatus);
        response.put("junggonara", junggonaraStatus);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 요청 데이터를 ProductData로 파싱
     */
    @SuppressWarnings("unchecked")
    private ProductData parseProductData(Map<String, Object> request) {
        return ProductData.builder()
            .name((String) request.get("name"))
            .description((String) request.get("description"))
            .price(((Number) request.get("price")).doubleValue())
            .category((String) request.get("category"))
            .location((String) request.get("location"))
            .condition((String) request.get("condition"))
            .images((java.util.List<String>) request.get("imagePaths"))
            .build();
    }
}
