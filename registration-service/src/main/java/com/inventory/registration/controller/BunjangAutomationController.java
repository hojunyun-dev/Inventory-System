package com.inventory.registration.controller;

import com.inventory.registration.model.AutomationResult;
import com.inventory.registration.model.ProductData;
import com.inventory.registration.service.BunjangAutomationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/automation/bunjang")
@RequiredArgsConstructor
@Slf4j
public class BunjangAutomationController {
    
    private final BunjangAutomationService bunjangAutomationService;
    
    @Value("${automation.test-accounts.naver.username:}")
    private String defaultNaverUsername;
    
    @Value("${automation.test-accounts.naver.password:}")
    private String defaultNaverPassword;
    
    /**
     * 번개장터 상품 등록 테스트
     */
    @PostMapping("/register")
    public ResponseEntity<AutomationResult> registerProduct(@RequestBody Map<String, Object> request) {
        try {
            log.info("번개장터 상품 등록 요청: {}", request);
            
            // 환경변수에서 네이버 계정 정보 우선 사용
            String username = defaultNaverUsername;
            String password = defaultNaverPassword;
            
            // 환경변수에 값이 없으면 요청 body에서 가져오기
            if ((username == null || username.isEmpty()) && request.get("username") != null) {
                username = (String) request.get("username");
            }
            if ((password == null || password.isEmpty()) && request.get("password") != null) {
                password = (String) request.get("password");
            }
            
            // 요청 데이터를 ProductData로 변환
            ProductData productData = ProductData.builder()
                    .name((String) request.get("name"))
                    .description((String) request.get("description"))
                    .price(Integer.parseInt(request.get("price").toString()))
                    .category((String) request.get("category"))
                    .condition((String) request.get("condition"))
                    .location((String) request.get("location"))
                    .username(username)
                    .password(password)
                    .build();
            
            // 번개장터 자동화 실행
            AutomationResult result = bunjangAutomationService.registerProductPublic(productData);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("번개장터 자동화 중 오류 발생: {}", e.getMessage(), e);
            AutomationResult errorResult = AutomationResult.builder()
                    .platform("bunjang")
                    .success(false)
                    .errorMessage("자동화 실행 중 오류 발생: " + e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(errorResult);
        }
    }
    
    /**
     * 번개장터 자동화 상태 확인
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(Map.of(
                "platform", "bunjang",
                "status", "available",
                "loginMethod", "네이버 OAuth 2.0",
                "description", "네이버 로그인을 통한 번개장터 자동화",
                "supportedFeatures", Map.of(
                        "productRegistration", true,
                        "categorySelection", true,
                        "imageUpload", true,
                        "priceNegotiation", true,
                        "directTrade", true
                )
        ));
    }
    
    /**
     * 번개장터 지원 카테고리 목록
     */
    @GetMapping("/categories")
    public ResponseEntity<Map<String, Object>> getCategories() {
        return ResponseEntity.ok(Map.of(
                "categories", new String[]{
                        "여성의류", "남성의류", "신발", "가방/지갑", "시계", "쥬얼리",
                        "패션 액세서리", "디지털", "가전제품", "스포츠/레저", "차량/오토바이",
                        "스타굿즈", "키덜트", "예술/희귀/수집품", "음반/악기", "도서/티켓/문구",
                        "뷰티/미용", "가구/인테리어", "생활/주방용품", "공구/산업용품",
                        "식품", "유아동/출산", "반려동물용품", "기타", "재능"
                },
                "totalCount", 25
        ));
    }
    
    /**
     * 번개장터 로그인 테스트 (네이버 OAuth)
     */
    @PostMapping("/test-login")
    public ResponseEntity<Map<String, Object>> testLogin(@RequestBody(required = false) Map<String, String> credentials) {
        log.info("번개장터 로그인 테스트 요청");
        
        // 환경변수에서 네이버 계정 정보 우선 사용
        String username = defaultNaverUsername;
        String password = defaultNaverPassword;
        
        // 환경변수에 값이 없으면 요청 body에서 가져오기
        if ((username == null || username.isEmpty()) && credentials != null) {
            username = credentials.get("username");
        }
        if ((password == null || password.isEmpty()) && credentials != null) {
            password = credentials.get("password");
        }
        
        log.info("사용할 네이버 계정: {}", username != null ? username.substring(0, Math.min(username.length(), 3)) + "***" : "null");
        
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "네이버 계정 정보가 설정되지 않았습니다. .env.local 파일 또는 요청 body에 계정 정보를 설정해주세요."
            ));
        }
        
        try {
            boolean loginSuccess = bunjangAutomationService.testLogin(username, password);
            
            if (loginSuccess) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "로그인 성공! 번개장터에 정상적으로 접속되었습니다.",
                    "platform", "bunjang",
                    "loginMethod", "네이버 OAuth 2.0",
                    "accountSource", "환경변수(.env.local)"
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "로그인 실패 - 네이버 계정 정보를 확인해주세요",
                    "platform", "bunjang",
                    "loginMethod", "네이버 OAuth 2.0"
                ));
            }
        } catch (Exception e) {
            log.error("번개장터 로그인 테스트 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "로그인 테스트 중 오류: " + e.getMessage(),
                "platform", "bunjang"
            ));
        }
    }
}
