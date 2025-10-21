package com.inventory.registration.service;

import com.inventory.registration.dto.ProductRegistrationRequest;
import com.inventory.registration.entity.ProductRegistration;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class BunjangRegistrationService {
    
    @Value("${platforms.bunjang.base-url}")
    private String bunjangBaseUrl;
    
    @Value("${automation.browser.headless}")
    private Boolean headless;
    
    @Value("${automation.browser.timeout}")
    private Integer timeout;
    
    private WebDriver webDriver;
    
    private synchronized WebDriver ensureDriver() {
        try {
            if (webDriver != null) {
                try {
                    webDriver.getCurrentUrl();
                    if (webDriver.getWindowHandles() == null || webDriver.getWindowHandles().isEmpty()) {
                        throw new WebDriverException("No window handles");
                    }
                    return webDriver;
                } catch (WebDriverException e) {
                    log.warn("Detected invalid/closed WebDriver session. Recreating...: {}", e.getMessage());
                    try { webDriver.quit(); } catch (Exception ignore) {}
                    webDriver = null;
                }
            }

            log.info("Creating new WebDriver instance...");
            WebDriverManager.chromedriver().setup();

            String baseProfile = "/home/code/.selenium-profiles/bunjang-session-" + System.currentTimeMillis();
            ChromeOptions options = new ChromeOptions();
            if (headless) {
                options.addArguments("--headless=new");
            }
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--user-data-dir=" + baseProfile);
            options.addArguments("--profile-directory=Default");
            options.addArguments("--disable-web-security");
            options.addArguments("--disable-features=VizDisplayCompositor");
            options.addArguments("--remote-allow-origins=*");
            options.addArguments("--disable-blink-features=AutomationControlled");
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            options.addArguments("--disable-mobile-emulation");
            options.addArguments("--disable-device-metrics");
            options.addArguments("--force-device-scale-factor=1");
            options.addArguments("--disable-web-security");
            options.addArguments("--disable-features=VizDisplayCompositor");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--disable-blink-features=AutomationControlled");
            options.addArguments("--disable-features=VizDisplayCompositor,TranslateUI,BlinkGenPropertyTrees");
            options.addArguments("--disable-ipc-flooding-protection");
            options.addArguments("--disable-renderer-backgrounding");
            options.addArguments("--disable-backgrounding-occluded-windows");
            options.addArguments("--disable-client-side-phishing-detection");
            options.addArguments("--disable-sync");
            options.addArguments("--disable-default-apps");
            options.addArguments("--disable-extensions");
            options.addArguments("--disable-plugins");
            options.addArguments("--disable-images");
            options.addArguments("--no-first-run");
            options.addArguments("--no-default-browser-check");
            options.addArguments("--lang=ko-KR");
            options.setExperimentalOption("excludeSwitches", java.util.List.of("enable-automation"));

                webDriver = new ChromeDriver(options);
            log.info("WebDriver created successfully");
            return webDriver;
        } catch (Exception e) {
            log.error("Failed to create WebDriver: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create WebDriver: " + e.getMessage());
        }
    }
    
    /**
     * 상품 등록 (자동화)
     */
    public ProductRegistration registerProduct(ProductRegistrationRequest request) {
        log.info("Starting product registration for: {}", request.getProductName());
        
        try {
            WebDriver driver = ensureDriver();
            
            // 로그인 상태 확인
            if (!isLoggedIn(driver)) {
                log.warn("Not logged in to Bunjang. Manual login required.");
                log.info("Please log in manually to Bunjang and then try again.");
                throw new RuntimeException("Manual login required. Please use /api/automation/bunjang/session/open to login manually.");
            }
            
            Map<String, Object> result = proceedWithProductRegistration(request);
            
            // ProductRegistration 객체 생성
            ProductRegistration registration = new ProductRegistration();
            registration.setPlatform("bunjang");
            registration.setProductId(request.getProductId());
            registration.setProductName(request.getProductName());
            registration.setProductDescription(request.getProductDescription());
            registration.setStatus((Boolean) result.get("success") ? "SUCCESS" : "FAILED");
            registration.setStartedAt(LocalDateTime.now());
            registration.setCompletedAt(LocalDateTime.now());
            registration.setErrorMessage((Boolean) result.get("success") ? null : (String) result.get("message"));
            registration.setRequestData(convertToJson(request));
            registration.setResponseData(convertToJson(result));
            
            return registration;
            
        } catch (Exception e) {
            log.error("Failed to register product: {}", e.getMessage());
            
            // 실패한 경우 ProductRegistration 객체 생성
            ProductRegistration registration = new ProductRegistration();
            registration.setPlatform("bunjang");
            registration.setProductId(request.getProductId());
            registration.setProductName(request.getProductName());
            registration.setProductDescription(request.getProductDescription());
            registration.setStatus("FAILED");
            registration.setStartedAt(LocalDateTime.now());
            registration.setCompletedAt(LocalDateTime.now());
            registration.setErrorMessage("Failed to register product: " + e.getMessage());
            registration.setRequestData(convertToJson(request));
            
            return registration;
        }
    }
    
    /**
     * 현재 로그인 상태 확인
     */
    public boolean isLoggedIn() {
        try {
            WebDriver driver = ensureDriver();
            return isLoggedIn(driver);
        } catch (Exception e) {
            log.error("Failed to check login status: {}", e.getMessage());
            return false;
        }
    }
    
    private boolean isLoggedIn(WebDriver driver) {
        try {
            log.info("Checking login status...");
            
            String currentUrl = driver.getCurrentUrl();
            log.info("Current URL: {}", currentUrl);
            
            // 로그인 버튼이 있는지 확인
            try {
                List<WebElement> loginButtons = driver.findElements(By.xpath("//a[contains(text(), '로그인') or contains(text(), '로그인/회원가입')]"));
                if (!loginButtons.isEmpty()) {
                    for (WebElement btn : loginButtons) {
                        if (btn.isDisplayed() && btn.isEnabled()) {
                            log.info("Login button found and visible - NOT logged in");
                            return false;
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("Error checking login buttons: {}", e.getMessage());
            }
            
            // 사용자 메뉴나 프로필 링크가 있는지 확인
            try {
                List<WebElement> userMenus = driver.findElements(By.xpath("//a[contains(@href, 'profile') or contains(@href, 'mypage') or contains(text(), '마이페이지') or contains(text(), '내정보')]"));
                if (!userMenus.isEmpty()) {
                    for (WebElement menu : userMenus) {
                        if (menu.isDisplayed() && menu.isEnabled()) {
                            log.info("User menu found and visible - logged in");
                            return true;
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("Error checking user menus: {}", e.getMessage());
            }
            
            // 페이지 소스에서 로그인 관련 키워드 확인
            String pageSource = driver.getPageSource();
            if (pageSource.contains("로그인") && pageSource.contains("회원가입")) {
                log.info("Login/register keywords found in page source - likely NOT logged in");
                return false;
            }
            
            // URL 패턴 확인
            if (currentUrl.contains("/login") || currentUrl.contains("/signin") || currentUrl.contains("/auth")) {
                log.info("On login/auth page - NOT logged in");
                return false;
            }
            
            log.info("No clear indicators of login status - assuming NOT logged in for safety");
            return false;
            
        } catch (Exception e) {
            log.error("Error checking login status: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 수동 로그인용 브라우저 창 열기
     */
    public void openForManualLogin() {
        openForManualLogin(null);
    }
    
    /**
     * 수동 로그인용 브라우저 창 열기 - 로그인 완료 후 상품 등록까지 자동 진행
     */
    public void openForManualLogin(ProductRegistrationRequest productRequest) {
        log.info("🚀 Starting Bunjang registration automation...");
        log.info("🔍 DEBUG: This is BunjangRegistrationService.openForManualLogin() method");
        WebDriver driver = null;
        try {
            driver = ensureDriver();
            
            // 브라우저 창 설정
            driver.manage().window().maximize();
            driver.switchTo().window(driver.getWindowHandle());
            
            log.info("🔍 DEBUG: Browser window opened successfully!");
            log.info("🔍 DEBUG: Current window handle: {}", driver.getWindowHandle());
            log.info("🔍 DEBUG: Number of windows: {}", driver.getWindowHandles().size());
            
            // 로그인 상태 확인
            boolean alreadyLoggedIn = isLoggedIn(driver);
            if (alreadyLoggedIn) {
                log.info("✅ Already logged in to Bunjang. Proceeding with product registration...");
                if (productRequest != null) {
                    proceedWithProductRegistration(productRequest);
                }
                return;
            }
            
            log.info("🔐 Not logged in. Starting login flow...");
            
            // 1단계: 번개장터 홈페이지 접속
            log.info("Step 1: Navigating to Bunjang homepage...");
            driver.get("https://www.bunjang.co.kr");
            Thread.sleep(5000);
            
            String currentUrl = driver.getCurrentUrl();
            log.info("Current URL: {}", currentUrl);
            
            // 네이버 로그인 페이지로 리다이렉트된 경우 강제로 번개장터 홈페이지로 이동
            if (currentUrl.contains("nid.naver.com") || currentUrl.contains("naver.com")) {
                log.warn("❌ Redirected to Naver login page! Force redirecting to Bunjang homepage...");
                driver.get("https://www.bunjang.co.kr");
                Thread.sleep(3000);
                currentUrl = driver.getCurrentUrl();
                log.info("After force redirect - Current URL: {}", currentUrl);
                
                // 여전히 네이버 페이지라면 JavaScript로 강제 리다이렉트
                if (currentUrl.contains("nid.naver.com") || currentUrl.contains("naver.com")) {
                    log.warn("❌ Still on Naver page! Using JavaScript to force redirect...");
                    ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("window.location.href = 'https://www.bunjang.co.kr';");
                    Thread.sleep(3000);
                    currentUrl = driver.getCurrentUrl();
                    log.info("After JavaScript redirect - Current URL: {}", currentUrl);
                }
            }
            
            // 2단계: 로그인 플로우 시작
            log.info("Step 2: Starting login flow...");
            
            // 네이버 페이지로 리다이렉트되는 것을 지속적으로 체크하고 방지
            int redirectAttempts = 0;
            while ((currentUrl.contains("nid.naver.com") || currentUrl.contains("naver.com")) && redirectAttempts < 5) {
                log.warn("❌ Detected Naver redirect attempt #{}! Force redirecting to Bunjang...", redirectAttempts + 1);
                driver.get("https://www.bunjang.co.kr");
                Thread.sleep(2000);
                currentUrl = driver.getCurrentUrl();
                redirectAttempts++;
                log.info("Redirect attempt #{} - Current URL: {}", redirectAttempts, currentUrl);
            }
            
            // 로그인 버튼 찾기
            WebElement loginButton = findLoginButton(driver);
            if (loginButton == null) {
                log.warn("❌ Could not find login button. Please login manually.");
                waitForManualLogin(driver, productRequest);
                return;
            }
            
            // 로그인 버튼 클릭
            log.info("Step 3: Clicking login button...");
                        loginButton.click();
                        Thread.sleep(3000);
                        
            // 팝업창에서 네이버 버튼 찾기
                        log.info("Step 4: Looking for Naver login button in popup...");
            WebElement naverButton = findNaverButton(driver);
            if (naverButton == null) {
                log.warn("❌ Could not find Naver button in popup. Please login manually.");
                waitForManualLogin(driver, productRequest);
                return;
            }
            
            // 네이버 버튼 클릭
            log.info("Step 5: Clicking Naver login button...");
                            naverButton.click();
            Thread.sleep(3000);
            
            log.info("✅ Successfully opened Naver login page!");
            log.info("👤 Please complete login manually in the browser window.");
            
            // 사용자 수동 로그인 대기
            waitForManualLogin(driver, productRequest);
                    
                } catch (Exception e) {
                    log.error("❌ Error during Bunjang login flow: {}", e.getMessage(), e);
            log.info("Please complete login manually in the browser window.");
            if (productRequest != null) {
                waitForManualLogin(driver, productRequest);
            }
        }
    }
    
    /**
     * 로그인 버튼 찾기
     */
    private WebElement findLoginButton(WebDriver driver) {
        log.info("Looking for login button...");
        
        String[] selectors = {
                            "//a[contains(text(), '로그인')]",
                            "//a[contains(text(), '회원가입')]",
                            "//button[contains(text(), '로그인')]",
                            "//button[contains(text(), '회원가입')]",
                            "//a[contains(@href, 'login')]",
                            "//a[contains(@class, 'login')]",
            "//button[contains(@class, 'login')]"
        };
        
        for (String selector : selectors) {
                            try {
                                List<WebElement> elements = driver.findElements(By.xpath(selector));
                for (WebElement element : elements) {
                    if (element.isDisplayed() && element.isEnabled()) {
                        String text = element.getText();
                        if (text.contains("로그인") || text.contains("회원가입")) {
                            log.info("✅ Found login button: {}", text);
                            return element;
                        }
                    }
                                }
                            } catch (Exception e) {
                                log.debug("Selector {} failed: {}", selector, e.getMessage());
                            }
                        }
                        
        log.warn("❌ Could not find login button");
        return null;
    }
    
    /**
     * 네이버 로그인 버튼 찾기
     */
    private WebElement findNaverButton(WebDriver driver) {
        log.info("Looking for Naver login button...");
        
        String[] selectors = {
            "//*[contains(text(), '네이버로 이용하기')]",
            "//*[contains(text(), '네이버')]",
                                "//button[contains(@class, 'naver')]",
            "//a[contains(@class, 'naver')]",
            "//a[contains(@href, 'naver')]"
        };
        
        for (String selector : selectors) {
                                try {
                                    List<WebElement> elements = driver.findElements(By.xpath(selector));
                for (WebElement element : elements) {
                    if (element.isDisplayed() && element.isEnabled()) {
                        String text = element.getText();
                        if (text.contains("네이버")) {
                            log.info("✅ Found Naver button: {}", text);
                            return element;
                        }
                    }
                }
                    } catch (Exception e) {
                log.debug("Selector {} failed: {}", selector, e.getMessage());
            }
        }
        
        log.warn("❌ Could not find Naver button");
        return null;
    }
    
    /**
     * 사용자 수동 로그인 대기
     */
    private void waitForManualLogin(WebDriver driver, ProductRegistrationRequest productRequest) {
        log.info("⏳ Waiting for manual login completion...");
        
        int maxWaitTime = 60; // 60초 대기
        for (int i = 0; i < maxWaitTime; i++) {
            try {
                    Thread.sleep(1000);
                if (isLoggedIn(driver)) {
                    log.info("✅ Login completed successfully!");
                    
                    if (productRequest != null) {
                        log.info("🚀 Proceeding with product registration...");
                        Map<String, Object> result = proceedWithProductRegistration(productRequest);
                        
                        if ((Boolean) result.get("success")) {
                            log.info("✅ Product registration completed: {}", result.get("message"));
            } else {
                            log.error("❌ Product registration failed: {}", result.get("message"));
                        }
                    }
                    return;
                        } else {
                    log.info("⏳ Still waiting for login... ({}/60)", i + 1);
                        }
                    } catch (Exception e) {
                log.warn("Error checking login status: {}", e.getMessage());
            }
        }
        
        log.warn("⏰ Timeout waiting for login. Please try again.");
    }
    
    /**
     * 상품 등록 진행
     */
    public Map<String, Object> proceedWithProductRegistration(ProductRegistrationRequest request) {
        log.info("🚀 Starting product registration process...");
        
        try {
            WebDriver driver = ensureDriver();
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            
            // 판매하기 버튼 클릭
            log.info("Step 1: Looking for '판매하기' button...");
            WebElement sellButton = findElementWithMultipleSelectors(driver, wait, 
                "//a[contains(text(), '판매하기')]", 
                "//button[contains(text(), '판매하기')]",
                "//a[contains(@href, 'products/new')]",
                "//a[contains(@href, 'sell')]"
            );
            
            if (sellButton != null) {
                log.info("✅ Found sell button, clicking...");
                sellButton.click();
                Thread.sleep(3000);
                    } else {
                log.warn("❌ Could not find sell button");
                throw new RuntimeException("판매하기 버튼을 찾을 수 없습니다.");
            }
            
            // 상품 등록 폼 작성
            log.info("Step 2: Filling product form...");
            fillProductForm(driver, wait, request);
            
            // 등록 버튼 클릭
            log.info("Step 3: Submitting product registration...");
            WebElement submitButton = findElementWithMultipleSelectors(driver, wait,
                "//button[contains(text(), '등록')]",
                "//button[contains(text(), '완료')]",
                "//button[@type='submit']",
                "//input[@type='submit']"
            );
            
            if (submitButton != null) {
                submitButton.click();
                            Thread.sleep(5000);
                log.info("✅ Product registration submitted successfully!");
            } else {
                log.error("❌ Could not find submit button");
                throw new RuntimeException("등록 버튼을 찾을 수 없습니다.");
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "상품이 성공적으로 등록되었습니다.");
            result.put("productName", request.getProductName());
            result.put("price", request.getPrice());
            
            return result;
            
                    } catch (Exception e) {
            log.error("Product registration failed: {}", e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "상품 등록에 실패했습니다: " + e.getMessage());
            return result;
        }
    }
    
    /**
     * 상품 등록 폼 작성
     */
    private void fillProductForm(WebDriver driver, WebDriverWait wait, ProductRegistrationRequest request) {
        log.info("Filling product form for: {}", request.getProductName());
        
        try {
            // 상품명 입력
            WebElement nameField = findElementWithMultipleSelectors(driver, wait, 
                "input[placeholder*='상품명']", 
                "input[name='name']", 
                "input[name='title']", 
                "input[placeholder*='제목']"
            );
            if (nameField != null) {
                nameField.clear();
                nameField.sendKeys(request.getProductName());
                log.info("✅ Product name filled: {}", request.getProductName());
            }
            
            // 상품 설명 입력
            WebElement descriptionField = findElementWithMultipleSelectors(driver, wait,
                "textarea[placeholder*='설명']", 
                "textarea[name='description']", 
                "textarea[name='content']"
            );
            if (descriptionField != null) {
                descriptionField.clear();
                descriptionField.sendKeys(request.getProductDescription());
                log.info("✅ Product description filled");
            }
            
            // 가격 입력
            WebElement priceField = findElementWithMultipleSelectors(driver, wait,
                "input[placeholder*='가격']", 
                "input[name='price']", 
                "input[type='number']"
            );
            if (priceField != null) {
                priceField.clear();
                priceField.sendKeys(String.valueOf(request.getPrice()));
                log.info("✅ Product price filled: {}", request.getPrice());
            }
            
            // 카테고리 선택 (필요한 경우)
            // 추가 필드들도 필요에 따라 구현
            
                } catch (Exception e) {
            log.error("Error filling product form: {}", e.getMessage());
            throw new RuntimeException("상품 정보 입력에 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 여러 선택자로 요소 찾기
     */
    private WebElement findElementWithMultipleSelectors(WebDriver driver, WebDriverWait wait, String... selectors) {
        for (String selector : selectors) {
            try {
                if (selector.startsWith("//")) {
                    // XPath 선택자
                    List<WebElement> elements = driver.findElements(By.xpath(selector));
                    for (WebElement element : elements) {
                        if (element.isDisplayed() && element.isEnabled()) {
                            return element;
                        }
                    }
                } else {
                    // CSS 선택자
                    List<WebElement> elements = driver.findElements(By.cssSelector(selector));
                    for (WebElement element : elements) {
                        if (element.isDisplayed() && element.isEnabled()) {
                    return element;
                }
                    }
                }
        } catch (Exception e) {
                log.debug("Selector {} failed: {}", selector, e.getMessage());
            }
        }
        return null;
    }
    
    /**
     * 세션/브라우저 닫기
     */
    public void closeSession() {
        log.info("Closing browser session...");
        
        try {
            if (webDriver != null) {
                webDriver.quit();
                webDriver = null;
                log.info("Browser session closed");
            }
                } catch (Exception e) {
            log.error("Failed to close browser session: {}", e.getMessage());
        }
    }
    
    /**
     * 객체를 JSON 문자열로 변환
     */
    private String convertToJson(Object obj) {
        // 간단한 JSON 변환 (실제로는 Jackson이나 Gson 사용 권장)
        return obj.toString();
    }
}