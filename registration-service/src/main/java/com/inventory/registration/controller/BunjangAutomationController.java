package com.inventory.registration.controller;

import com.inventory.registration.service.BunjangRegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/automation/bunjang")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class BunjangAutomationController {

    private final BunjangRegistrationService bunjangRegistrationService;

    /**
     * 수동 로그인용 브라우저 창 열기
     */
    @PostMapping("/session/open")
    public ResponseEntity<Map<String, Object>> openManualLoginWindow() {
        try {
            Map<String, Object> result = bunjangRegistrationService.openForManualLogin(null);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("수동 로그인 창 오픈 실패: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "수동 로그인 창 오픈 실패: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 상품 정보와 함께 로그인 시작 (로그인 완료 후 자동 상품 등록)
     */
    @PostMapping("/session/open-with-product")
    public ResponseEntity<Map<String, Object>> openManualLoginWithProduct(@RequestBody Map<String, Object> request) {
        try {
            log.info("상품 정보와 함께 로그인 시작: {}", request);
            
            // 요청 데이터를 ProductRegistrationRequest로 변환
            com.inventory.registration.dto.ProductRegistrationRequest productRequest = 
                new com.inventory.registration.dto.ProductRegistrationRequest();
            productRequest.setPlatform("bunjang");
            productRequest.setProductId(String.valueOf(request.get("productId")));
            productRequest.setProductName((String) request.get("productName"));
            productRequest.setProductDescription((String) request.get("description"));
            if (request.get("price") != null) {
                productRequest.setPrice(Double.valueOf(request.get("price").toString()));
            }
            if (request.get("quantity") != null) {
                productRequest.setStock(Integer.valueOf(request.get("quantity").toString()));
            }
            productRequest.setCategory((String) request.get("category"));
            
            // 상품 정보와 함께 로그인 시작
            bunjangRegistrationService.openForManualLogin(productRequest);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "브라우저가 열렸습니다. 번개장터에 로그인한 후 상품이 자동으로 등록됩니다."
            ));
        } catch (Exception e) {
            log.error("상품 정보와 함께 로그인 시작 실패: {}", e.getMessage(), e);
            log.error("Exception details: ", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "로그인 시작 실패: " + e.getMessage(),
                "error", e.getClass().getSimpleName(),
                "details", e.getMessage()
            ));
        }
    }

    /**
     * 현재 로그인 상태 확인
     */
    @GetMapping("/session/status")
    public ResponseEntity<Map<String, Object>> sessionStatus() {
        try {
            Map<String, Object> result = bunjangRegistrationService.checkLoginStatus();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("로그인 상태 확인 실패: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "로그인 상태 확인 실패: " + e.getMessage()
            ));
        }
    }

    /**
     * 세션/브라우저 닫기
     */
    @PostMapping("/session/close")
    public ResponseEntity<Map<String, Object>> closeSession() {
        try {
            bunjangRegistrationService.cleanup();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "세션을 종료했습니다."
            ));
        } catch (Exception e) {
            log.error("세션 종료 실패: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "세션 종료 실패: " + e.getMessage()
            ));
        }
    }

    /**
     * 브라우저 세션 유지
     */
    @PostMapping("/session/keep-alive")
    public ResponseEntity<Map<String, Object>> keepSessionAlive() {
        try {
            // 세션 상태 확인으로 세션 유지 효과
            Map<String, Object> result = bunjangRegistrationService.checkLoginStatus();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "브라우저 세션이 유지되었습니다."
            ));
        } catch (Exception e) {
            log.error("세션 유지 실패: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "세션 유지 실패: " + e.getMessage()
            ));
        }
    }

    /**
     * 브라우저 세션 상태 확인
     */
    @GetMapping("/session/active")
    public ResponseEntity<Map<String, Object>> sessionActive() {
        try {
            Map<String, Object> result = bunjangRegistrationService.checkLoginStatus();
            boolean isActive = (Boolean) result.get("loggedIn");
            return ResponseEntity.ok(Map.of(
                "success", true,
                "active", isActive,
                "message", isActive ? "브라우저 세션이 활성화되어 있습니다." : "브라우저 세션이 비활성화되어 있습니다."
            ));
        } catch (Exception e) {
            log.error("세션 상태 확인 실패: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "세션 상태 확인 실패: " + e.getMessage()
            ));
        }
    }

    /**
     * 로그인 후 상품 등록 자동 진행
     */
    @PostMapping("/register-after-login")
    public ResponseEntity<Map<String, Object>> registerProductAfterLogin(@RequestBody Map<String, Object> request) {
        try {
            log.info("상품 등록 요청 받음 (로그인 후): {}", request);
            
            // 요청 데이터 변환
            com.inventory.registration.dto.ProductRegistrationRequest registrationRequest = new com.inventory.registration.dto.ProductRegistrationRequest();
            registrationRequest.setPlatform("bunjang");
            registrationRequest.setProductId(String.valueOf(request.get("productId")));
            registrationRequest.setProductName((String) request.get("productName"));
            registrationRequest.setProductDescription((String) request.get("description"));
            if (request.get("price") != null) {
                registrationRequest.setPrice(Double.valueOf(request.get("price").toString()));
            }
            if (request.get("quantity") != null) {
                registrationRequest.setStock(Integer.valueOf(request.get("quantity").toString()));
            }
            registrationRequest.setCategory((String) request.get("category"));
            
            // 상품 등록 실행 (로그인 후 자동 진행)
            Map<String, Object> result = bunjangRegistrationService.proceedWithProductRegistration(registrationRequest);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("상품 등록 실패 (로그인 후): {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "상품 등록 실패: " + e.getMessage()
            ));
        }
    }

    /**
     * 상품 등록
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerProduct(@RequestBody Map<String, Object> request) {
        try {
            log.info("상품 등록 요청 받음: {}", request);
            
            // 요청 데이터 변환 (DTO 필드명에 맞게 매핑)
            com.inventory.registration.dto.ProductRegistrationRequest registrationRequest = new com.inventory.registration.dto.ProductRegistrationRequest();
            registrationRequest.setPlatform("bunjang");
            registrationRequest.setProductId(String.valueOf(request.get("productId")));
            registrationRequest.setProductName((String) request.get("productName"));
            registrationRequest.setProductDescription((String) request.get("description"));
            if (request.get("price") != null) {
                registrationRequest.setPrice(Double.valueOf(request.get("price").toString()));
            }
            if (request.get("quantity") != null) {
                registrationRequest.setStock(Integer.valueOf(request.get("quantity").toString()));
            }
            registrationRequest.setCategory((String) request.get("category"));
            // images: List<String> or String[] 처리
            Object imagesObj = request.get("images");
            if (imagesObj instanceof java.util.List<?> list) {
                String[] arr = list.stream().filter(java.util.Objects::nonNull).map(Object::toString).toArray(String[]::new);
                registrationRequest.setImages(arr);
            } else if (imagesObj instanceof String[]) {
                registrationRequest.setImages((String[]) imagesObj);
            }
            
            // 상품 등록 실행
            com.inventory.registration.entity.ProductRegistration result = 
                bunjangRegistrationService.registerProduct(registrationRequest);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "상품 등록이 완료되었습니다.",
                "registrationId", result != null && result.getPlatformProductId() != null ? result.getPlatformProductId() : "unknown",
                "platformUrl", result != null && result.getPlatformUrl() != null ? result.getPlatformUrl() : "unknown"
            ));
        } catch (Exception e) {
            log.error("상품 등록 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "상품 등록 실패: " + e.getMessage()
            ));
        }
    }
}