package com.inventory.registration.service;

import com.inventory.registration.dto.ProductRegistrationRequest;
import com.inventory.registration.entity.ProductRegistration;
import com.inventory.registration.service.bunjang.*;
import com.example.common.dto.TokenBundle;
import com.example.common.dto.ProductRegisterRequest;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 번개장터 상품 등록 메인 서비스 클래스
 * - 분리된 클래스들을 조합하여 전체 자동화 프로세스 관리
 * - WebDriver, 로그인, 폼 처리를 각각의 전문 클래스에 위임
 */
@Service
@Slf4j
public class BunjangRegistrationService {
    
    @Value("${platforms.bunjang.base-url}")
    private String bunjangBaseUrl;
    
    @Value("${automation.browser.headless}")
    private Boolean headless;
    
    @Autowired
    private BunjangWebDriverManager webDriverManager;
    
    @Autowired
    private BunjangLoginHandler loginHandler;
    
    @Autowired
    private AwsIpRotationService awsIpRotationService;
    
    @Autowired
    private BunjangApiRegistrationService apiRegistrationService;
    
    @Autowired
    private BunjangUtils utils;
    
    @Autowired
    private BunjangTokenCapturer tokenCapturer;
    
    @Autowired
    private TokenBundleService tokenBundleService;
    
    private WebDriver webDriver;
    
    /**
     * WebDriver 인스턴스 생성 및 관리
     */
    private synchronized WebDriver ensureDriver() {
        try {
            // 세션 유효성 검사를 완화하여 브라우저가 열려있으면 재사용
            if (webDriver != null) {
                try {
                    // 간단한 URL 확인만으로 세션 유효성 판단
                    String currentUrl = webDriver.getCurrentUrl();
                    if (currentUrl != null && !currentUrl.isEmpty()) {
                        // 로그인 상태 확인 전에 홈페이지로 이동하여 상태 새로고침
                        try {
                            if (!currentUrl.contains("bunjang.co.kr")) {
                                webDriver.get("https://m.bunjang.co.kr/");
                                Thread.sleep(1000); // 페이지 로딩 대기
                            }
                        } catch (Exception e) {
                            log.warn("홈페이지 이동 중 오류: {}", e.getMessage());
                        }
                        
                        // 🚨 로그아웃 플래그 확인 및 토큰 삭제
                        checkAndHandleLogoutFlag(webDriver);
                        
                        // 로그인 상태 확인: 로그아웃 상태라면 세션 종료하고 새로 생성
                        boolean isLoggedIn = loginHandler.isLoggedIn(webDriver);
                        if (!isLoggedIn) {
                            log.info("⚠️ 브라우저 세션이 로그아웃된 상태입니다. 기존 세션을 종료하고 새로 생성합니다.");
                            
                            // 로그아웃 상태이면 DB 토큰도 삭제
                            try {
                                TokenBundle tb = tokenBundleService.getTokenBundle("BUNJANG");
                                if (tb != null) {
                                    log.info("🚨 브라우저에서 로그아웃 상태 감지! DB 토큰을 삭제합니다.");
                                    tokenBundleService.deleteTokenBundle("BUNJANG");
                                }
                            } catch (Exception e) {
                                log.warn("토큰 삭제 중 오류: {}", e.getMessage());
                            }
                            
                            try {
                                webDriver.quit();
                            } catch (Exception e) {
                                log.warn("기존 WebDriver 종료 중 오류: {}", e.getMessage());
                            }
                            webDriver = null;
                            // 새 세션 생성으로 진행
                        } else {
                            log.info("✅ Reusing existing browser session (URL: {})", currentUrl);
                            return webDriver;
                        }
                    }
                } catch (Exception e) {
                    log.warn("⚠️ Existing browser session is invalid, creating new one: {}", e.getMessage());
                    try {
                        if (webDriver != null) {
                            webDriver.quit();
                        }
                    } catch (Exception ex) {
                        log.warn("WebDriver 종료 중 오류: {}", ex.getMessage());
                    }
                    webDriver = null;
                }
            }
            
            log.info("Creating new WebDriver instance...");
            webDriver = webDriverManager.createWebDriver();
            return webDriver;
            
        } catch (Exception e) {
            log.error("Failed to create WebDriver: {}", e.getMessage());
            throw new RuntimeException("WebDriver creation failed", e);
        }
    }
    
    /**
     * 수동 로그인을 위한 브라우저 열기
     */
    public Map<String, Object> openForManualLogin(ProductRegistrationRequest productRequest) {
        log.info("🚀 Starting Bunjang registration automation...");
            WebDriver driver = ensureDriver();

        // ✅ 로그인 되었는지 엄격한 확인
        boolean isLoggedIn = loginHandler.isLoggedIn(driver);
        log.info("Initial login status: {}", isLoggedIn);
        
        if (!isLoggedIn) {
            log.info("🔐 Not logged in. Running login flow...");
            try {
                // 1단계: 홈페이지에서 로그인/회원가입 버튼 클릭
                log.info("🔍 Looking for login/register button on homepage...");
                WebElement loginButton = loginHandler.findLoginButton(driver);
                if (loginButton != null) {
                    log.info("✅ Found login button, clicking...");
                    utils.humanClick(driver, loginButton);
                    Thread.sleep(3000); // 팝업창 로딩 대기
                    log.info("✅ Login button clicked, popup should appear");
                } else {
                    log.warn("❌ Login button not found, trying direct navigation...");
                    loginHandler.navigateToLoginPage(driver);
                }
                
                // 2단계: 팝업창에서 '네이버로 이용하기' 버튼 클릭
                log.info("🔍 Looking for '네이버로 이용하기' button in popup...");
                WebElement naverButton = loginHandler.findNaverButton(driver);
                if (naverButton != null) {
                    log.info("✅ Found Naver login button in popup, clicking...");
                    utils.humanClick(driver, naverButton);
                    Thread.sleep(3000); // 네이버 로그인 페이지 로딩 대기
                    log.info("✅ Naver login button clicked successfully");
                } else {
                    log.warn("❌ Naver login button not found in popup, trying alternative methods...");
                }
                
                log.info("Login flow completed, waiting for login completion...");
                
                // ✅ 로그인 완료 대기 (사용자 수동 로그인 대기)
                log.info("⏳ Waiting for user to complete manual login...");
                if (!loginHandler.waitForLoginComplete(driver, 60)) {  // 60초 대기
                    log.error("❌ Login completion timeout - user did not complete login");
                    return Map.of("success", false, "message", "사용자 로그인 완료 대기 시간 초과 (60초)");
                }
                
                // ✅ 로그인 상태 재확인
                isLoggedIn = loginHandler.isLoggedIn(driver);
                log.info("Final login status after manual login: {}", isLoggedIn);
                
                if (!isLoggedIn) {
                    return Map.of("success", false, "message", "로그인 상태 재확인 실패");
                }
                
                log.info("✅ Manual login completed successfully!");
                
                // 차단/봇감지 확인 및 롤백 처리
                boolean rollbackPerformed = loginHandler.handleBlockingAndRollback(driver, webDriverManager);
                if (rollbackPerformed) {
                    log.info("🔄 Rollback performed, retrying login flow...");
                    // 롤백 후 다시 로그인 플로우 시도
                    loginHandler.runLoginFlow(driver, webDriverManager);
                }
                
                // 성공 시 에러 카운트 리셋
                awsIpRotationService.resetErrorCount();
                    
                } catch (Exception e) {
                log.error("Login flow failed: {}", e.getMessage());
                
                // 차단 감지 처리
                boolean shouldReboot = awsIpRotationService.handleBlockingDetection(e.getMessage());
                if (shouldReboot) {
                    return Map.of("success", false, "message", "차단 감지로 인한 EC2 리부트 필요: " + e.getMessage());
                }
                
                return Map.of("success", false, "message", "로그인 플로우 실행 실패: " + e.getMessage());
            }
            
            // 로그인 완료 대기 로직은 위에서 이미 처리됨
            
            // ✅ 로그인 완료 후 토큰 캡처 수행
            log.info("🔍 Login completed! Capturing authentication token...");
            
            // x-bun-auth-token 추출 (1순위)
            String authToken = tokenCapturer.captureAuthToken(driver);
            if (authToken == null) {
                log.error("❌ x-bun-auth-token capture failed - no fallback to CSRF");
                return Map.of(
                    "success", false, 
                    "message", "x-bun-auth-token 캡처 실패 - CSRF 토큰과 혼용 금지"
                );
            }
            
            // CSRF 토큰도 별도로 캡처
            String csrfToken = tokenCapturer.captureToken(driver);
            
            // authToken과 csrf가 같은 경우 실패 처리
            if (authToken.equals(csrfToken)) {
                log.error("❌ authToken equals CSRF token - this is invalid: {}", authToken.substring(0, 8) + "...");
                return Map.of(
                    "success", false, 
                    "message", "authToken과 CSRF 토큰이 동일함 - 잘못된 토큰 캡처"
                );
            }
            
            log.info("✅ x-bun-auth-token captured successfully: {}", authToken.substring(0, 8) + "...");
            if (csrfToken != null) {
                log.info("✅ CSRF token captured: {}", csrfToken.substring(0, 8) + "...");
            }
            
            // 쿠키 캡처
            List<com.example.common.dto.CookieEntry> capturedCookies = tokenCapturer.captureCookies(driver);
            log.info("🍪 Captured {} cookies", capturedCookies.size());
            
            // 토큰을 TokenBundleService에 저장
            try {
                TokenBundle tokenBundle = new TokenBundle();
                tokenBundle.platform = "BUNJANG";
                tokenBundle.csrf = csrfToken; // CSRF 토큰 (별도)
                tokenBundle.authToken = authToken; // x-bun-auth-token (별도)
                tokenBundle.expiresAt = Instant.now().plus(Duration.ofHours(9)); // 9시간 후 만료
                tokenBundle.cookies = capturedCookies; // 실제 쿠키 리스트
                
                tokenBundleService.saveTokenBundle(tokenBundle);
                log.info("✅ Token and cookies saved to TokenBundleService successfully");
                
                // 파일 저장 완료를 위한 짧은 대기
                Thread.sleep(100);
                
                // 상품등록 API 호출
                try {
                    log.info("🚀 Starting automatic product registration via API...");
                    ProductRegisterRequest apiRequest = new ProductRegisterRequest();
                    apiRequest.platform = "BUNJANG";
                    apiRequest.productId = String.valueOf(productRequest.getProductId());
                    apiRequest.name = productRequest.getProductName();
                    apiRequest.price = productRequest.getPrice().longValue();
                    apiRequest.description = productRequest.getProductDescription();
                    apiRequest.categoryId = productRequest.getCategory();
                    apiRequest.keywords = List.of(productRequest.getCategory());
                    
                    Map<String, Object> apiResult = apiRegistrationService.registerProduct(apiRequest).block();
                    log.info("✅ Product registration API call completed: {}", apiResult);
                    
                    return Map.of(
                        "success", true, 
                        "message", "로그인 완료, 토큰 캡처 및 상품등록 API 호출 성공",
                        "token", authToken,
                        "apiResult", apiResult
                    );
                } catch (Exception e) {
                    log.error("❌ Product registration API call failed: {}", e.getMessage());
                    return Map.of(
                        "success", false, 
                        "message", "로그인 완료 및 토큰 캡처 성공, 상품등록 API 호출 실패: " + e.getMessage(),
                        "token", authToken
                    );
                }
            } catch (Exception e) {
                log.warn("⚠️ Failed to save token to TokenBundleService: {}", e.getMessage());
                return Map.of(
                    "success", false, 
                    "message", "토큰 저장 실패: " + e.getMessage()
                );
            }
        }

        log.info("✅ Login confirmed. Starting product registration with existing session...");
        
        // 이미 로그인된 상태에서 상품등록 API 직접 호출 (DB에 저장된 토큰 사용)
        try {
            log.info("🚀 Starting product registration via API with existing token...");
            ProductRegisterRequest apiRequest = new ProductRegisterRequest();
            apiRequest.platform = "BUNJANG";
            apiRequest.productId = String.valueOf(productRequest.getProductId());
            apiRequest.name = productRequest.getProductName();
            apiRequest.price = productRequest.getPrice().longValue();
            apiRequest.description = productRequest.getProductDescription();
            apiRequest.categoryId = productRequest.getCategory();
            apiRequest.keywords = List.of(productRequest.getCategory());
            
            Map<String, Object> apiResult = apiRegistrationService.registerProduct(apiRequest).block();
            log.info("✅ Product registration API call completed: {}", apiResult);
            
            return Map.of(
                "success", true,
                "message", "기존 세션으로 상품등록 완료",
                "apiResult", apiResult
            );
        } catch (Exception e) {
            log.error("❌ Product registration failed: {}", e.getMessage());
            return Map.of(
                "success", false, 
                "message", "상품등록 실패: " + e.getMessage()
            );
        }
    }
    
    /**
     * 로그인 상태 확인
     */
    public Map<String, Object> checkLoginStatus() {
        try {
            WebDriver driver = ensureDriver();
            boolean isLoggedIn = loginHandler.isLoggedIn(driver);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("loggedIn", isLoggedIn);
            result.put("message", isLoggedIn ? "로그인됨" : "로그인되지 않음");
            
            return result;
            
            } catch (Exception e) {
            log.error("❌ 로그인 상태 확인 실패: {}", e.getMessage());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("loggedIn", false);
            result.put("message", "로그인 상태 확인 실패: " + e.getMessage());
            
            return result;
        }
    }

    /**
     * 브라우저에서 로그아웃 플래그 확인 및 토큰 삭제
     */
    private void checkAndHandleLogoutFlag(WebDriver driver) {
        if (driver == null) {
            return;
        }
        
        try {
            // JavaScript로 localStorage에서 로그아웃 플래그 확인
            JavascriptExecutor js = (JavascriptExecutor) driver;
            Object flagValue = js.executeScript("return localStorage.getItem('__BUN_LOGOUT_FLAG__');");
            
            if (flagValue != null && "true".equals(flagValue.toString())) {
                log.info("🚨 브라우저에서 로그아웃 감지됨! DB 토큰을 삭제합니다.");
                tokenBundleService.deleteTokenBundle("BUNJANG");
                
                // 플래그 초기화
                js.executeScript("localStorage.removeItem('__BUN_LOGOUT_FLAG__');");
                log.info("✅ 로그아웃 플래그 초기화 완료");
            }
        } catch (Exception e) {
            log.debug("로그아웃 플래그 확인 중 오류 (무시): {}", e.getMessage());
        }
    }
    
    /**
     * 경량 로그인 상태 확인 (WebDriver 실제 상태 확인 포함)
     */
    public Map<String, Object> checkLoginStatusLight() {
        try {
            // 1. 토큰 기반 확인
            TokenBundle tb = tokenBundleService.getTokenBundle("BUNJANG");
            boolean hasToken = (tb != null) && !tokenBundleService.isExpired(tb) && tb.authToken != null;
            
            // 2. 브라우저 실제 상태 확인 (토큰이 있어도 브라우저에서 로그아웃했을 수 있음)
            // 기존 webDriver 필드가 있을 때만 브라우저 상태 확인 (경량 확인이므로 새 세션 생성하지 않음)
            boolean browserLoggedIn = false;
            WebDriver currentDriver = null;
            try {
                // 기존 webDriver 필드가 있는 경우만 확인 (새로 생성하지 않음)
                if (webDriver != null) {
                    try {
                        // 세션 유효성 확인
                        String currentUrl = webDriver.getCurrentUrl();
                        if (currentUrl != null && !currentUrl.isEmpty()) {
                            currentDriver = webDriver;
                            
                            // 🚨 로그아웃 플래그 확인 및 토큰 삭제
                            checkAndHandleLogoutFlag(webDriver);
                            
                            // 홈페이지로 이동하여 상태 새로고침 (로그아웃 확인)
                            try {
                                webDriver.get("https://m.bunjang.co.kr/");
                                Thread.sleep(1000); // 페이지 로딩 대기
                            } catch (Exception e) {
                                log.debug("홈페이지 이동 중 오류 (무시): {}", e.getMessage());
                            }
                            
                            // 로그아웃 플래그 다시 확인 (페이지 로드 후)
                            checkAndHandleLogoutFlag(webDriver);
                            
                            // 브라우저 실제 로그인 상태 확인
                            browserLoggedIn = loginHandler.isLoggedIn(webDriver);
                            log.info("브라우저 로그인 상태: {}", browserLoggedIn);
                            
                            // 로그아웃 상태이면 토큰 삭제
                            if (!browserLoggedIn && hasToken) {
                                log.info("🚨 브라우저에서 로그아웃된 상태 감지! DB 토큰을 삭제합니다.");
                                tokenBundleService.deleteTokenBundle("BUNJANG");
                                hasToken = false; // 토큰 삭제 후 상태 업데이트
                            }
                        } else {
                            log.debug("기존 브라우저 세션의 URL을 가져올 수 없음");
                        }
                    } catch (Exception e) {
                        log.debug("기존 브라우저 세션 확인 중 오류: {}", e.getMessage());
                        // 세션이 무효하면 null로 처리
                        currentDriver = null;
                    }
                } else {
                    log.debug("기존 브라우저 세션이 없음 (경량 확인 모드)");
                }
            } catch (Exception e) {
                log.debug("브라우저 상태 확인 중 오류 (토큰만 확인): {}", e.getMessage());
                // 브라우저 확인 실패 시 토큰 기반으로만 판단
            }
            
            // 3. 로그인 상태 판단
            // - 브라우저 세션이 없으면 무조건 false (실제 브라우저 상태를 확인할 수 없으므로)
            // - 브라우저 세션이 있으면 브라우저 실제 상태로 판단
            boolean logged;
            if (currentDriver != null) {
                // 브라우저가 있으면 브라우저 실제 상태가 우선 (로그아웃 시 false)
                logged = browserLoggedIn;
                log.info("브라우저 세션이 존재하므로 브라우저 로그인 상태 기준: {}", logged);
            } else {
                // 브라우저 세션이 없으면 무조건 false (실제 상태 확인 불가)
                logged = false;
                log.info("브라우저 세션이 없으므로 로그인되지 않은 것으로 판단 (실제 상태 확인 불가)");
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("loggedIn", logged);
            
            if (logged) {
                if (currentDriver != null && browserLoggedIn) {
                    result.put("message", "토큰 및 브라우저 세션 모두 유효");
                } else {
                    // 이 케이스는 발생하지 않아야 함 (브라우저가 없으면 logged = false)
                    result.put("message", "토큰 기반 로그인 유효 (브라우저 세션 없음)");
                }
            } else {
                if (currentDriver != null && !browserLoggedIn) {
                    result.put("message", "브라우저에서 로그아웃된 상태");
                } else if (currentDriver == null) {
                    result.put("message", "브라우저 세션이 없습니다. 로그인이 필요합니다.");
                } else if (!hasToken) {
                    result.put("message", "토큰 부재 또는 만료");
                } else {
                    result.put("message", "로그인되지 않음");
                }
            }
            
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("loggedIn", false);
            result.put("message", "경량 로그인 확인 실패: " + e.getMessage());
            return result;
        }
    }
    
    /**
     * 상품 등록 API 엔드포인트 (자동화)
     */
    public ProductRegistration registerProduct(ProductRegistrationRequest request) {
        log.info("Starting product registration for: {}", request.getProductName());
        
        try {
            // ProductRegistrationRequest를 ProductRegisterRequest로 변환
            com.example.common.dto.ProductRegisterRequest apiRequest = new com.example.common.dto.ProductRegisterRequest();
            apiRequest.name = request.getProductName();
            apiRequest.description = request.getProductDescription();
            apiRequest.price = request.getPrice().longValue();
            apiRequest.categoryId = request.getCategory();
            apiRequest.keywords = List.of(); // 기본값
            
            // API 기반 상품 등록 실행
            Map<String, Object> result = apiRegistrationService.registerProduct(apiRequest).block();
            
            if (result != null && (Boolean) result.get("success")) {
                log.info("✅ Product registration successful via API");
                
                // ProductRegistration 객체 생성
                ProductRegistration registration = new ProductRegistration();
                registration.setPlatform("bunjang");
                registration.setProductId(request.getProductId());
                registration.setPlatformProductId((String) result.get("registrationId"));
                registration.setProductName(request.getProductName());
                registration.setProductDescription(request.getProductDescription());
                registration.setPlatformUrl((String) result.get("platformUrl"));
                
                return registration;
            } else {
                throw new RuntimeException("상품 등록 실패: " + (result != null ? result.get("message") : "API 호출 실패"));
            }
            
        } catch (Exception e) {
            log.error("❌ 상품 등록 중 오류 발생: {}", e.getMessage());
            
            // 인증 오류 (401/403) 또는 "No valid token" 오류 시 토큰 삭제
            String errorMessage = e.getMessage() != null ? e.getMessage() : "";
            if (errorMessage.contains("401") || errorMessage.contains("403") || 
                errorMessage.contains("No valid token") || errorMessage.contains("인증")) {
                log.warn("🚨 인증 오류 감지. DB 토큰을 삭제합니다.");
                try {
                    tokenBundleService.deleteTokenBundle("BUNJANG");
                    log.info("✅ 토큰 삭제 완료");
                } catch (Exception deleteEx) {
                    log.warn("토큰 삭제 중 오류: {}", deleteEx.getMessage());
                }
            }
            
            throw new RuntimeException("상품 등록 실패: " + e.getMessage());
        }
    }
    
    
    /**
     * WebDriver 정리
     */
    public void cleanup() {
        try {
            if (webDriver != null) {
                webDriverManager.quitWebDriver();
                webDriver = null;
            }
                } catch (Exception e) {
            log.warn("WebDriver cleanup failed: {}", e.getMessage());
        }
    }
}