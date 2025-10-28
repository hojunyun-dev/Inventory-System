package com.inventory.registration.service.bunjang;

import com.example.common.dto.ProductRegisterRequest;
import com.example.common.dto.TokenBundle;
import com.inventory.registration.service.bunjang.BunjangTokenCapturer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * 통합 번개장터 서비스
 * - 기존 로그인 플로우 활용
 * - 로그인 완료 후 토큰 캡처
 * - API 기반 상품 등록
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IntegratedBunjangService {

    private final BunjangLoginHandler loginHandler;
    private final CdpTokenCaptureService tokenCaptureService;
    private final BunjangTokenCapturer tokenCapturer;
    private final TokenBundleService tokenBundleService;
    private final BunjangApiRegistrationService apiRegistrationService;

    @Value("${chrome.for-testing.path:/home/code/chrome-for-testing/chrome-linux64/chrome}")
    private String chromeForTestingPath;

    @Value("${chrome.driver.path:/home/code/chrome-for-testing/chromedriver-linux64/chromedriver}")
    private String chromeDriverPath;

    /**
     * 로그인 및 토큰 캡처 통합 플로우
     */
    public Map<String, Object> loginAndCaptureToken() {
        WebDriver driver = null;
        try {
            log.info("🚀 Starting integrated login and token capture flow...");
            
            // 1. Chrome for Testing 설정
            ChromeOptions options = new ChromeOptions();
            options.setBinary(chromeForTestingPath);
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-blink-features=AutomationControlled");
            options.addArguments("--user-agent=Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Mobile Safari/537.36");
            
            System.setProperty("webdriver.chrome.driver", chromeDriverPath);
            driver = new ChromeDriver(options);
            
            // 2. 번개장터 홈페이지로 이동
            driver.get("https://m.bunjang.co.kr/");
            log.info("📱 Navigated to Bunjang mobile homepage");
            
            // 3. 기존 로그인 플로우 실행
            try {
                loginHandler.runLoginFlow(driver, null);
                log.info("✅ Login flow completed");
            } catch (Exception e) {
                log.error("❌ Login failed: {}", e.getMessage());
                return Map.of(
                    "success", false,
                    "message", "Login failed: " + e.getMessage()
                );
            }
            
            log.info("✅ Login completed successfully");
            
            // 4. 로그인 완료 후 토큰 캡처 (기존 방식 사용)
            String token = tokenCapturer.captureToken(driver);
            
            if (token != null && !token.isEmpty()) {
                // 5. 토큰을 TokenBundle 형태로 변환하여 저장
                TokenBundle tokenBundle = new TokenBundle(
                    "BUNJANG",
                    List.of(new com.example.common.dto.CookieEntry("x-bun-auth-token", token)),
                    null,
                    java.time.Instant.now().plusSeconds(8 * 3600) // 8시간 후 만료
                );
                
                tokenBundleService.saveTokenBundle(tokenBundle);
                
                log.info("✅ Token captured and saved successfully");
                return Map.of(
                    "success", true,
                    "message", "Login and token capture completed successfully",
                    "token", maskToken(token),
                    "expiresAt", tokenBundle.expiresAt
                );
            } else {
                log.error("❌ Token capture failed");
                return Map.of(
                    "success", false,
                    "message", "Token capture failed"
                );
            }
            
        } catch (Exception e) {
            log.error("❌ Integrated flow failed: {}", e.getMessage(), e);
            return Map.of(
                "success", false,
                "message", "Integrated flow failed: " + e.getMessage()
            );
        } finally {
            // 브라우저 유지 - 사용자가 수동으로 닫을 때까지 열어둠
            if (driver != null) {
                log.info("🌐 Browser kept open for manual inspection. Please close manually when done.");
                // driver.quit() 제거하여 브라우저 유지
            }
        }
    }

    /**
     * 상품 등록 (토큰이 없으면 자동 로그인 후 등록)
     */
    public Map<String, Object> registerProduct(ProductRegisterRequest request) {
        try {
            log.info("📦 Starting product registration: {}", request.name);
            
            // 1. 토큰 상태 확인
            TokenBundle existingToken = tokenBundleService.getTokenBundle("BUNJANG");
            if (existingToken == null || tokenBundleService.isExpired(existingToken)) {
                log.info("🔄 No valid token found, performing login and token capture...");
                
                // 2. 로그인 및 토큰 캡처
                Map<String, Object> loginResult = loginAndCaptureToken();
                if (!(Boolean) loginResult.get("success")) {
                    return Map.of(
                        "success", false,
                        "message", "Login failed: " + loginResult.get("message")
                    );
                }
            }
            
            // 3. API 기반 상품 등록
            return apiRegistrationService.registerProduct(request)
                .map(response -> Map.of(
                    "success", true,
                    "message", "Product registered successfully",
                    "response", response
                ))
                .onErrorReturn(Map.of(
                    "success", false,
                    "message", "Product registration failed"
                ))
                .block();
                
        } catch (Exception e) {
            log.error("❌ Product registration failed: {}", e.getMessage(), e);
            return Map.of(
                "success", false,
                "message", "Product registration failed: " + e.getMessage()
            );
        }
    }

    /**
     * 토큰 상태 조회
     */
    public Map<String, Object> getTokenStatus() {
        return apiRegistrationService.getTokenStatus();
    }

    /**
     * 토큰 삭제
     */
    public Map<String, Object> deleteToken() {
        try {
            tokenBundleService.deleteTokenBundle("BUNJANG");
            return Map.of(
                "success", true,
                "message", "Token deleted successfully"
            );
        } catch (Exception e) {
            log.error("❌ Failed to delete token: {}", e.getMessage(), e);
            return Map.of(
                "success", false,
                "message", "Failed to delete token: " + e.getMessage()
            );
        }
    }

    /**
     * 토큰 마스킹 (보안을 위해 일부만 표시)
     */
    private String maskToken(String token) {
        if (token == null || token.length() <= 8) {
            return token;
        }
        return token.substring(0, 8) + "...";
    }
}
