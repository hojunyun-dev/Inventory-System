package com.inventory.registration.service;

import com.inventory.registration.constants.PlatformConstants;
import com.inventory.registration.model.AutomationResult;
import com.inventory.registration.model.ProductData;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
// WebDriverManager 제거 - Selenium Manager 사용
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Value;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class BaseAutomationService {
    
    @Autowired
    protected ObjectFactory<WebDriver> webDriverFactory;
    protected WebDriver webDriver;
    
    @Value("${automation.retry.max-attempts:3}")
    protected int maxRetryAttempts;
    
    @Value("${automation.retry.delay:2000}")
    protected long retryDelayMs;
    
    @Value("${automation.screenshot.enabled:true}")
    protected boolean screenshotEnabled;
    
    @Value("${automation.screenshot.path:/tmp/screenshots}")
    protected String screenshotPath;
    
    protected WebDriverWait wait;
    
    @PostConstruct
    public void init() {
        setupScreenshotDirectory();
        log.info("BaseAutomationService initialized for platform: {}", getPlatformName());
    }
    
    @PreDestroy
    public void cleanup() {
        if (webDriver != null) {
            try {
                webDriver.quit();
                log.info("WebDriver cleaned up for platform: {}", getPlatformName());
            } catch (Exception e) {
                log.error("Error during WebDriver cleanup: {}", e.getMessage());
            }
        }
    }
    
    // 추상 메서드 - 각 플랫폼별로 구현
    protected abstract String getPlatformName();
    protected abstract String getLoginUrl();
    protected abstract String getRegisterUrl();
    
    // 공통 메서드들
    protected boolean login(String username, String password) {
        try {
            log.info("Starting login process for platform: {}", getPlatformName());
            ensureDriver();
            webDriver.get(getLoginUrl());
            
            // 로그인 폼 입력
            if (fillLoginForm(username, password)) {
                // 로그인 성공 확인
                return waitForLoginSuccess();
            }
            
            return false;
        } catch (Exception e) {
            log.error("Login failed for platform {}: {}", getPlatformName(), e.getMessage());
            takeScreenshot("login_error");
            return false;
        }
    }
    
    protected abstract boolean fillLoginForm(String username, String password);
    protected abstract boolean waitForLoginSuccess();
    
    protected AutomationResult registerProduct(ProductData productData) {
        AutomationResult.AutomationResultBuilder resultBuilder = AutomationResult.builder()
                .platform(getPlatformName())
                .startedAt(LocalDateTime.now())
                .success(false);
        
        try {
            log.info("Starting product registration for platform: {}", getPlatformName());
            
            // 상품 등록 페이지로 이동
            navigateToProductRegistration();
            
            // 상품 정보 입력
            if (fillProductForm(productData)) {
                // 상품 등록 제출
                if (submitProduct()) {
                    // 등록 성공 확인
                    String productUrl = waitForRegistrationSuccess();
                    if (productUrl != null) {
                        resultBuilder.success(true)
                                .status("SUCCESS")
                                .productUrl(productUrl);
                    } else {
                        resultBuilder.errorMessage("Failed to get product URL after registration");
                    }
                } else {
                    resultBuilder.errorMessage("Failed to submit product");
                }
            } else {
                resultBuilder.errorMessage("Failed to fill product form");
            }
            
        } catch (Exception e) {
            log.error("Product registration failed for platform {}: {}", getPlatformName(), e.getMessage());
            resultBuilder.errorMessage(e.getMessage())
                    .errorCode("REGISTRATION_ERROR");
            takeScreenshot("registration_error");
        } finally {
            resultBuilder.markAsCompleted();
        }
        
        return resultBuilder.build();
    }
    
    protected void navigateToProductRegistration() {
        ensureDriver();
        webDriver.get(getRegisterUrl());
        waitForPageLoad();
    }
    
    protected abstract boolean fillProductForm(ProductData productData);
    protected abstract boolean submitProduct();
    protected abstract String waitForRegistrationSuccess();
    
    // 유틸리티 메서드들
    protected void waitForPageLoad() {
        try {
            ensureDriver();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            Thread.sleep(1000); // 추가 대기
        } catch (Exception e) {
            log.warn("Page load wait interrupted: {}", e.getMessage());
        }
    }
    
    protected boolean waitForElement(By locator, long timeoutSeconds) {
        try {
            WebDriverWait customWait = new WebDriverWait(webDriver, Duration.ofSeconds(timeoutSeconds));
            customWait.until(ExpectedConditions.presenceOfElementLocated(locator));
            return true;
        } catch (TimeoutException e) {
            log.warn("Element not found: {}", locator);
            return false;
        }
    }
    
    protected void safeClick(WebElement element) {
        try {
            // 스크롤하여 요소가 보이도록 함
            ((JavascriptExecutor) webDriver).executeScript("arguments[0].scrollIntoView(true);", element);
            Thread.sleep(500);
            
            // 클릭 시도
            element.click();
        } catch (Exception e) {
            // JavaScript 클릭으로 재시도
            try {
                ((JavascriptExecutor) webDriver).executeScript("arguments[0].click();", element);
            } catch (Exception e2) {
                log.error("Failed to click element: {}", e2.getMessage());
                throw e2;
            }
        }
    }
    
    protected void safeInput(WebElement element, String text) {
        try {
            element.clear();
            element.sendKeys(text);
        } catch (Exception e) {
            log.error("Failed to input text to element: {}", e.getMessage());
            throw e;
        }
    }
    
    protected void takeScreenshot(String prefix) {
        if (!screenshotEnabled) return;
        
        try {
            String timestamp = LocalDateTime.now().toString().replace(":", "-");
            String filename = String.format("%s_%s_%s.png", prefix, getPlatformName(), timestamp);
            Path filePath = Paths.get(screenshotPath, filename);
            
            Files.createDirectories(filePath.getParent());
            
            TakesScreenshot screenshot = (TakesScreenshot) webDriver;
            byte[] screenshotBytes = screenshot.getScreenshotAs(OutputType.BYTES);
            Files.write(filePath, screenshotBytes);
            
            log.info("Screenshot saved: {}", filePath.toString());
        } catch (IOException e) {
            log.error("Failed to take screenshot: {}", e.getMessage());
        }
    }
    
    protected void setupScreenshotDirectory() {
        try {
            Path path = Paths.get(screenshotPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                log.info("Created screenshot directory: {}", screenshotPath);
            }
        } catch (IOException e) {
            log.error("Failed to create screenshot directory: {}", e.getMessage());
        }
    }
    
    protected boolean handleCaptcha() {
        // 기본적으로 CAPTCHA 처리는 수동으로 처리하도록 함
        log.warn("CAPTCHA detected. Manual intervention may be required.");
        takeScreenshot("captcha_detected");
        
        // 사용자에게 알림 (실제 구현에서는 웹소켓이나 다른 방법으로 알림)
        try {
            Thread.sleep(30000); // 30초 대기
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    protected void retryWithDelay(Runnable action, int maxAttempts) {
        for (int i = 0; i < maxAttempts; i++) {
            try {
                action.run();
                return;
            } catch (Exception e) {
                if (i == maxAttempts - 1) {
                    throw e;
                }
                log.warn("Attempt {} failed, retrying in {}ms: {}", i + 1, retryDelayMs, e.getMessage());
                try {
                    Thread.sleep(retryDelayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrupted", ie);
                }
            }
        }
    }

    protected void ensureDriver() {
        log.info("ensureDriver() 진입 - 현재 webDriver: {}", (this.webDriver == null ? "null" : "present"));
        if (this.webDriver == null) {
            log.info("Creating WebDriver instance lazily for platform: {}", getPlatformName());
            WebDriver created = null;
            try {
                created = webDriverFactory.getObject();
            } catch (Exception e) {
                log.warn("Failed to create WebDriver from factory, falling back to direct ChromeDriver: {}", e.getMessage());
                try {
                    // Selenium Manager가 자동으로 ChromeDriver 관리
                    ChromeOptions options = new ChromeOptions();
                    options.addArguments("--no-sandbox");
                    options.addArguments("--disable-dev-shm-usage");
                    options.addArguments("--window-size=1920,1080");
                    created = new ChromeDriver(options);
                } catch (Exception e2) {
                    log.error("Fallback ChromeDriver creation failed: {}", e2.getMessage());
                    throw e2;
                }
            }
            if (created == null) {
                throw new IllegalStateException("WebDriver factory returned null");
            }
            this.webDriver = created;
            this.wait = new WebDriverWait(webDriver, Duration.ofSeconds(PlatformConstants.DEFAULT_TIMEOUT / 1000));
            log.info("ensureDriver() 완료 - webDriver 생성됨");
        }
    }
}
