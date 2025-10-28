package com.inventory.registration.service;

import com.inventory.registration.dto.ProductRegistrationRequest;
import com.inventory.registration.entity.ProductRegistration;
import com.inventory.registration.service.bunjang.*;
import com.example.common.dto.TokenBundle;
import com.example.common.dto.ProductRegisterRequest;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
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
    private BunjangFormHandler formHandler;
    
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
                        log.info("✅ Reusing existing browser session (URL: {})", currentUrl);
                        return webDriver;
                    }
                } catch (Exception e) {
                    log.warn("⚠️ Existing browser session is invalid, creating new one: {}", e.getMessage());
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
                        "success", true, 
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

        log.info("✅ Login confirmed. Capturing token from existing session...");
        
        // 이미 로그인된 상태에서도 토큰 캡처 시도
        String capturedToken = tokenCapturer.captureToken(driver);
        if (capturedToken != null && tokenCapturer.isValidToken(capturedToken)) {
            log.info("✅ Token captured from existing session");
            return Map.of(
                "success", true, 
                "message", "기존 세션에서 토큰 캡처 성공",
                "token", capturedToken
            );
        } else {
            log.warn("⚠️ Token capture failed from existing session");
            return Map.of(
                "success", false, 
                "message", "기존 세션에서 토큰 캡처 실패"
            );
        }
    }
    
    /**
     * 판매하기 버튼을 클릭하여 상품 등록 페이지로 이동 (강화된 버전) - DEPRECATED
     */
    @Deprecated
    private boolean navigateToProductRegistrationPage(WebDriver driver) {
        log.info("🔍 Starting enhanced navigation to product registration page...");
        
        // 판매하기 버튼 셀렉터 디버깅
        loginHandler.debugSellButtonSelectors(driver);
        
        // 1차 시도: 판매하기 버튼 클릭
        if (trySellButtonClick(driver)) {
            return true;
        }
        
        // 2차 시도: 직접 URL 이동
        if (tryDirectUrlNavigation(driver)) {
            return true;
        }
        
        // 3차 시도: 홈페이지에서 판매하기 버튼 재시도
        if (tryHomepageSellButton(driver)) {
            return true;
        }
        
        log.error("❌ All navigation attempts failed");
        return false;
    }
    
    /**
     * 판매하기 버튼 클릭 시도 (모바일 페이지 지원)
     */
    private boolean trySellButtonClick(WebDriver driver) {
        log.info("🎯 Attempt 1: Trying sell button click...");
        
        try {
            // 브라우저 세션 유효성 확인 및 복구
            if (!isBrowserSessionValid(driver)) {
                log.warn("Browser session is invalid, attempting recovery...");
                if (!recoverSession(driver)) {
                    log.error("Session recovery failed, skipping sell button click");
                    return false;
                }
            }
            
            // 실제 HTML 구조에 맞춘 최적화된 셀렉터들 (모바일 페이지 지원)
            String[] sellSelectors = {
                // 가장 정확한 셀렉터 (우선순위 1) - class와 텍스트 모두 확인
                "//a[contains(@class,'sc-eXEjpC') and contains(text(),'판매하기')]",
                // 모바일 페이지용 정확한 셀렉터 (우선순위 2)
                "//a[@class='sc-eXEjpC BltZS' and contains(text(),'판매하기')]",
                // href 기반 셀렉터 (우선순위 3) - 매우 정확함
                "//a[contains(@href,'products/new')]",
                // 이미지 alt 텍스트 기반 셀렉터 (우선순위 4)
                "//a[contains(text(),'판매하기') and .//img[@alt='판매하기버튼 이미지']]",
                // 텍스트 기반 셀렉터 (우선순위 5) - 일반적
                "//a[contains(text(),'판매하기')]",
                // 모든 요소에서 찾기 (우선순위 6) - 백업용
                "//*[contains(text(),'판매하기')]"
            };
            
            WebElement sellButton = null;
            for (String selector : sellSelectors) {
                try {
                    sellButton = new WebDriverWait(driver, Duration.ofSeconds(2))
                        .until(ExpectedConditions.elementToBeClickable(By.xpath(selector)));
                    log.info("✅ Found sell button with selector: {}", selector);
                    break;
                            } catch (Exception e) {
                    log.debug("Sell button not found with selector: {}", selector);
                }
            }
            
            if (sellButton != null) {
                log.info("🖱️ Clicking sell button...");
                
                // 자연스러운 클릭
                utils.humanFocusAndScroll(driver, sellButton);
                utils.humanClick(driver, sellButton);
                    Thread.sleep(3000);
                
                // 팝업 창 관리 개선
                String originalWindow = driver.getWindowHandle();
                try {
                    // 새 창이 열렸는지 확인
                    if (driver.getWindowHandles().size() > 1) {
                        log.info("🔄 New window detected, switching to product registration page...");
                        
                        // 새 창으로 전환
                        for (String windowHandle : driver.getWindowHandles()) {
                            if (!windowHandle.equals(originalWindow)) {
                                driver.switchTo().window(windowHandle);
                                String currentUrl = driver.getCurrentUrl();
                                log.info("Switched to window with URL: {}", currentUrl);
                                
                                if (currentUrl.contains("products/new")) {
                                    log.info("✅ Successfully switched to product registration window");
                                    break;
                                }
                            }
                        }
                } else {
                        // 같은 창에서 페이지가 변경된 경우
                        log.info("📄 Page changed in same window");
                        Thread.sleep(2000); // 페이지 로딩 대기
                }
                } catch (Exception e) {
                    log.warn("Window switching failed, trying to recover: {}", e.getMessage());
                    // 팝업이 닫힌 경우 원래 창으로 복귀
                    try {
                        driver.switchTo().window(originalWindow);
                        log.info("🔄 Recovered to original window");
                    } catch (Exception recoveryError) {
                        log.error("Failed to recover to original window: {}", recoveryError.getMessage());
                    }
                }
                
                // 상품 등록 폼 확인
                if (isProductFormPresent(driver)) {
                    log.info("✅ Sell button click successful - product form detected");
                    return true;
            } else {
                    log.warn("⚠️ Sell button clicked but product form not detected, URL: {}", driver.getCurrentUrl());
                                }
                            }
                        } catch (Exception e) {
            log.warn("Sell button click failed: {}", e.getMessage()); 
        }
        
        return false;
    }
    
    /**
     * 직접 URL 이동 시도 (모바일 페이지 지원)
     */
    private boolean tryDirectUrlNavigation(WebDriver driver) {
        log.info("🎯 Attempt 2: Trying direct URL navigation...");
        
        try {
            // 브라우저 세션 유효성 확인 및 복구
            if (!isBrowserSessionValid(driver)) {
                log.warn("Browser session is invalid, attempting recovery...");
                if (!recoverSession(driver)) {
                    log.error("Session recovery failed, skipping direct URL navigation");
                    return false;
                }
            }
            
            // 현재 URL이 모바일인지 확인
            String currentUrl = driver.getCurrentUrl();
            String targetUrl = currentUrl.contains("m.bunjang.co.kr") 
                ? "https://m.bunjang.co.kr/products/new"
                : "https://www.bunjang.co.kr/products/new";
                
            log.info("Navigating to: {}", targetUrl);
            driver.navigate().to(targetUrl);
                    Thread.sleep(3000);
            
            // 페이지 로드 완료 대기
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(driver1 -> {
                JavascriptExecutor js = (JavascriptExecutor) driver1;
                return js.executeScript("return document.readyState").equals("complete");
            });
            
            if (isProductFormPresent(driver)) {
                log.info("✅ Direct URL navigation successful");
                return true;
                    }
                } catch (Exception e) {
            log.warn("Direct URL navigation failed: {}", e.getMessage()); 
        }
        
        return false;
    }
    
    /**
     * 홈페이지에서 판매하기 버튼 재시도
     */
    private boolean tryHomepageSellButton(WebDriver driver) {
        log.info("🎯 Attempt 3: Trying homepage sell button...");
        
        try {
            // 브라우저 세션 유효성 확인 및 복구
            if (!isBrowserSessionValid(driver)) {
                log.warn("Browser session is invalid, attempting recovery...");
                if (!recoverSession(driver)) {
                    log.error("Session recovery failed, skipping homepage sell button attempt");
                    return false;
                }
            }
            
            // 현재 URL이 모바일인지 확인하여 적절한 홈페이지로 이동
            String currentUrl = driver.getCurrentUrl();
            String homepageUrl = currentUrl.contains("m.bunjang.co.kr") 
                ? "https://m.bunjang.co.kr"
                : "https://www.bunjang.co.kr";
                
            log.info("Navigating to homepage: {}", homepageUrl);
            driver.navigate().to(homepageUrl);
            Thread.sleep(3000);
            
            // 로그인 상태 재확인
            if (!loginHandler.isLoggedIn(driver)) {
                log.warn("Not logged in on homepage, skipping sell button attempt");
                return false;
            }
            
            // 판매하기 버튼 셀렉터 디버깅
            loginHandler.debugSellButtonSelectors(driver);
            
            // 판매하기 버튼 클릭 재시도
            return trySellButtonClick(driver);
            
        } catch (Exception e) {
            log.warn("Homepage sell button attempt failed: {}", e.getMessage());
        }
        
        return false;
    }
    
    /**
     * 브라우저 세션 유효성 확인 및 복구
     */
    private boolean isBrowserSessionValid(WebDriver driver) {
        try {
            // WebDriverManager를 통한 세션 유효성 검사
            if (!webDriverManager.isSessionValid(driver)) {
                log.warn("Browser session is invalid, attempting recovery...");
                return false;
            }
            
            // 간단한 명령으로 세션 유효성 확인
            String currentUrl = driver.getCurrentUrl();
            log.debug("Session valid, current URL: {}", currentUrl);
                return true;
                } catch (Exception e) {
            log.warn("Browser session is invalid: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 세션 복구 시도 (WebDriver 자동 복구 포함)
     */
    private boolean recoverSession(WebDriver driver) {
        log.info("🔄 Attempting session recovery...");
        
        try {
            // 1. 현재 창 핸들 확인
            String currentWindow = driver.getWindowHandle();
            log.info("Current window handle: {}", currentWindow);
            
            // 2. 모든 창 핸들 확인
            java.util.Set<String> allWindows = driver.getWindowHandles();
            log.info("Total windows: {}", allWindows.size());
            
            // 3. 유효한 창으로 전환 시도
            for (String windowHandle : allWindows) {
                try {
                    driver.switchTo().window(windowHandle);
                    String url = driver.getCurrentUrl();
                    log.info("Window {} URL: {}", windowHandle, url);
                    
                    // 번개장터 도메인인 경우 복구 성공
                    if (url.contains("bunjang.co.kr")) {
                        log.info("✅ Session recovered to bunjang window");
                        return true;
                                    }
                                } catch (Exception e) {
                    log.debug("Window {} is invalid: {}", windowHandle, e.getMessage());
                }
            }
            
            // 4. 모든 창이 유효하지 않은 경우 홈페이지로 이동
            log.info("All windows invalid, navigating to bunjang homepage...");
            driver.navigate().to("https://m.bunjang.co.kr");
            Thread.sleep(3000);
            
            return true;
            
        } catch (Exception e) {
            log.error("Session recovery failed: {}", e.getMessage());
            
            // 5. WebDriver 자체가 죽은 경우 WebDriverManager를 통한 복구 시도
            log.info("🔄 Attempting WebDriver-level recovery...");
            try {
                WebDriver recoveredDriver = webDriverManager.recoverWebDriver();
                if (recoveredDriver != null) {
                    // 복구된 드라이버를 현재 세션에 반영
                    log.info("✅ WebDriver recovery successful");
                    return true;
                }
            } catch (Exception recoveryError) {
                log.error("WebDriver recovery also failed: {}", recoveryError.getMessage());
            }
            
            return false;
        }
    }
    
    /**
     * 상품 등록 폼이 현재 페이지에 존재하는지 확인
     */
    private boolean isProductFormPresent(WebDriver driver) {
        return utils.existsDisplayed(driver, By.cssSelector("form")) &&
               (utils.existsDisplayed(driver, By.cssSelector("form input[name*='title'], form input[placeholder*='상품']"))
                || utils.existsDisplayed(driver, By.cssSelector("form textarea")));
    }
    
    /**
     * 상품 등록 프로세스 진행
     */
    public Map<String, Object> proceedWithProductRegistration(ProductRegistrationRequest request) {
            WebDriver driver = ensureDriver();
        // 🩹 안전장치: 다시 한 번 로그인 확인 (직접 호출 시 대비)
        if (!loginHandler.isLoggedIn(driver)) {
            return Map.of("success", false, "message", "로그인되지 않았습니다.");
        }
        WebDriverWait wait = webDriverManager.createWebDriverWait(driver, 10);

        // 페이지 보장
        if (!driver.getCurrentUrl().contains("/products/new")) {
            if (!formHandler.goToProductNew(driver)) {
                return Map.of("success", false, "message", "상품 등록 페이지 진입 실패");
            }
        }

        formHandler.fillProductForm(driver, wait, request);
        // (선택) formHandler.submitAndWaitSuccess(driver);

        // 등록 결과(실제 성공 신호/URL 필요 시 보강)
        String regId = "bunjang_" + System.currentTimeMillis();
        return Map.of("success", true, "message", "상품 등록 입력 완료", "registrationId", regId, "platformUrl", driver.getCurrentUrl());
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