package com.inventory.registration.service;

import com.inventory.registration.model.AutomationResult;
import com.inventory.registration.model.ProductData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "selenium.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class NaverSocialAutomationService {
    
    private final ObjectFactory<WebDriver> webDriverFactory;
    private final NaverOAuthService naverOAuthService;
    
    private WebDriver getWebDriver() {
        return webDriverFactory.getObject();
    }
    
    @Value("${platforms.bunjang.base-url}")
    private String bunjangBaseUrl;
    
    @Value("${platforms.junggonara.base-url:https://cafe.naver.com/joonggonara}")
    private String junggonaraBaseUrl;
    
    /**
     * 네이버 로그인으로 번개장터 자동화
     */
    public AutomationResult registerProductToBunjangWithNaver(ProductData productData, String accessToken) {
        LocalDateTime startedAt = LocalDateTime.now();
        String screenshot = null;
        String productUrl = null;
        String errorMessage = null;
        boolean success = false;
        
        try {
            log.info("네이버 로그인으로 번개장터 상품 등록 시작: {}", productData.getName());
            
            WebDriver webDriver = getWebDriver();
            
            // 1. 번개장터 로그인 페이지로 이동
            webDriver.get(bunjangBaseUrl + "/login");
            
            // 2. 네이버 로그인 버튼 클릭
            WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(10));
            WebElement naverLoginButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='naver']"))
            );
            naverLoginButton.click();
            
            // 3. 네이버 OAuth 인증 처리
            if (!handleNaverOAuth(webDriver, accessToken)) {
                errorMessage = "네이버 OAuth 인증 실패";
                screenshot = takeScreenshot(webDriver, "naver_oauth_failure");
                throw new RuntimeException(errorMessage);
            }
            
            // 4. 번개장터 상품 등록 페이지로 이동
            webDriver.get(bunjangBaseUrl + "/products/new");
            
            // 5. 상품 정보 입력
            if (!fillBunjangProductForm(webDriver, productData)) {
                errorMessage = "번개장터 상품 폼 입력 실패";
                screenshot = takeScreenshot(webDriver, "bunjang_form_failure");
                throw new RuntimeException(errorMessage);
            }
            
            // 6. 상품 등록
            if (!submitBunjangProduct(webDriver)) {
                errorMessage = "번개장터 상품 등록 실패";
                screenshot = takeScreenshot(webDriver, "bunjang_submit_failure");
                throw new RuntimeException(errorMessage);
            }
            
            // 7. 등록 성공 확인
            productUrl = verifyBunjangRegistration(webDriver);
            if (productUrl == null) {
                errorMessage = "번개장터 등록 성공 확인 실패";
                screenshot = takeScreenshot(webDriver, "bunjang_verify_failure");
                throw new RuntimeException(errorMessage);
            }
            
            success = true;
            log.info("네이버 로그인으로 번개장터 상품 등록 성공: {}", productUrl);
            
        } catch (Exception e) {
            log.error("네이버 로그인 번개장터 자동화 실패: {}", e.getMessage());
                if (screenshot == null) {
                try {
                    WebDriver webDriver = getWebDriver();
                    screenshot = takeScreenshot(webDriver, "bunjang_general_error");
                } catch (Exception ex) {
                    log.error("스크린샷 촬영 실패: {}", ex.getMessage());
                }
            }
            if (errorMessage == null) {
                errorMessage = e.getMessage();
            }
        }
        
        return AutomationResult.builder()
                .platform("bunjang")
                .success(success)
                .productUrl(productUrl)
                .errorMessage(errorMessage)
                .startedAt(startedAt)
                .screenshotPath(screenshot)
                .markAsCompleted()
                .build();
    }
    
    /**
     * 네이버 로그인으로 중고나라 자동화
     */
    public AutomationResult registerProductToJunggonaraWithNaver(ProductData productData, String accessToken) {
        LocalDateTime startedAt = LocalDateTime.now();
        String screenshot = null;
        String productUrl = null;
        String errorMessage = null;
        boolean success = false;
        
        try {
            log.info("네이버 로그인으로 중고나라 상품 등록 시작: {}", productData.getName());
            
            WebDriver webDriver = getWebDriver();
            
            // 1. 중고나라 카페로 이동
            webDriver.get(junggonaraBaseUrl);
            
            // 2. 네이버 로그인 확인 (이미 로그인된 상태인지 확인)
            if (!isNaverLoggedIn(webDriver)) {
                // 3. 네이버 로그인 페이지로 직접 이동하지 않고 번개장터 플로우 사용
                log.info("Not logged in to Naver. Please use Bunjang login flow instead.");
                throw new RuntimeException("네이버 로그인 페이지로 직접 이동하지 않습니다. 번개장터 로그인 플로우를 사용하세요.");
            }
            
            // 5. 중고나라 카페 글쓰기 페이지로 이동
            webDriver.get(junggonaraBaseUrl + "/ArticleWrite.nhn?clubid=10050146");
            
            // 6. 상품 정보 입력
            if (!fillJunggonaraProductForm(webDriver, productData)) {
                errorMessage = "중고나라 상품 폼 입력 실패";
                screenshot = takeScreenshot(webDriver, "junggonara_form_failure");
                throw new RuntimeException(errorMessage);
            }
            
            // 7. 상품 등록
            if (!submitJunggonaraProduct(webDriver)) {
                errorMessage = "중고나라 상품 등록 실패";
                screenshot = takeScreenshot(webDriver, "junggonara_submit_failure");
                throw new RuntimeException(errorMessage);
            }
            
            // 8. 등록 성공 확인
            productUrl = verifyJunggonaraRegistration(webDriver);
            if (productUrl == null) {
                errorMessage = "중고나라 등록 성공 확인 실패";
                screenshot = takeScreenshot(webDriver, "junggonara_verify_failure");
                throw new RuntimeException(errorMessage);
            }
            
            success = true;
            log.info("네이버 로그인으로 중고나라 상품 등록 성공: {}", productUrl);
            
        } catch (Exception e) {
            log.error("네이버 로그인 중고나라 자동화 실패: {}", e.getMessage());
            if (screenshot == null) {
                try {
                    WebDriver webDriver = getWebDriver();
                    screenshot = takeScreenshot(webDriver, "junggonara_general_error");
                } catch (Exception ex) {
                    log.error("스크린샷 촬영 실패: {}", ex.getMessage());
                }
            }
            if (errorMessage == null) {
                errorMessage = e.getMessage();
            }
        }
        
        return AutomationResult.builder()
                .platform("junggonara")
                .success(success)
                .productUrl(productUrl)
                .errorMessage(errorMessage)
                .startedAt(startedAt)
                .screenshotPath(screenshot)
                .markAsCompleted()
                .build();
    }
    
    /**
     * 네이버 OAuth 인증 처리
     */
    private boolean handleNaverOAuth(WebDriver webDriver, String accessToken) {
        try {
            // 이미 액세스 토큰이 있으므로 직접 사용자 정보 조회
            naverOAuthService.getUserInfo(accessToken).block();
            
            // JavaScript를 통해 네이버 세션 설정 (실제 구현에서는 더 복잡할 수 있음)
            String script = String.format(
                "localStorage.setItem('naver_access_token', '%s'); " +
                "sessionStorage.setItem('naver_logged_in', 'true');", 
                accessToken
            );
            ((JavascriptExecutor) webDriver).executeScript(script);
            
            return true;
        } catch (Exception e) {
            log.error("네이버 OAuth 인증 처리 실패: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 네이버 로그인 상태 확인
     */
    private boolean isNaverLoggedIn(WebDriver webDriver) {
        try {
            // 네이버 로그인 상태를 확인하는 JavaScript 실행
            Object result = ((JavascriptExecutor) webDriver).executeScript(
                "return sessionStorage.getItem('naver_logged_in') === 'true';"
            );
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.warn("네이버 로그인 상태 확인 실패: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 번개장터 상품 폼 입력
     */
    private boolean fillBunjangProductForm(WebDriver webDriver, ProductData productData) {
        try {
            WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(10));
            
            // 상품명 입력
            WebElement nameInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[name='name']"))
            );
            nameInput.clear();
            nameInput.sendKeys(productData.getName());
            
            // 가격 입력
            WebElement priceInput = webDriver.findElement(By.cssSelector("input[name='price']"));
            priceInput.clear();
            priceInput.sendKeys(productData.getPrice().toString());
            
            // 설명 입력
            WebElement descriptionTextarea = webDriver.findElement(By.cssSelector("textarea[name='description']"));
            descriptionTextarea.clear();
            descriptionTextarea.sendKeys(productData.getDescription());
            
            // 카테고리 선택
            if (productData.getCategory() != null) {
                WebElement categorySelect = webDriver.findElement(By.cssSelector("select[name='category']"));
                categorySelect.sendKeys(productData.getCategory());
            }
            
            // 위치 입력
            if (productData.getLocation() != null) {
                WebElement locationInput = webDriver.findElement(By.cssSelector("input[name='location']"));
                locationInput.clear();
                locationInput.sendKeys(productData.getLocation());
            }
            
            // 이미지 업로드
            if (productData.getImagePaths() != null && !productData.getImagePaths().isEmpty()) {
                WebElement fileInput = webDriver.findElement(By.cssSelector("input[type='file']"));
                fileInput.sendKeys(productData.getImagePaths().get(0));
            }
            
            return true;
        } catch (Exception e) {
            log.error("번개장터 상품 폼 입력 실패: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 번개장터 상품 등록
     */
    private boolean submitBunjangProduct(WebDriver webDriver) {
        try {
            WebElement submitButton = webDriver.findElement(By.cssSelector("button[type='submit']"));
            submitButton.click();
            
            // 등록 완료까지 대기
            WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(30));
            wait.until(ExpectedConditions.urlContains("/products/"));
            
            return true;
        } catch (Exception e) {
            log.error("번개장터 상품 등록 실패: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 번개장터 등록 성공 확인
     */
    private String verifyBunjangRegistration(WebDriver webDriver) {
        try {
            String currentUrl = webDriver.getCurrentUrl();
            if (currentUrl.contains("/products/")) {
                return currentUrl;
            }
            return null;
        } catch (Exception e) {
            log.error("번개장터 등록 성공 확인 실패: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 중고나라 상품 폼 입력
     */
    private boolean fillJunggonaraProductForm(WebDriver webDriver, ProductData productData) {
        try {
            WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(10));
            
            // 제목 입력
            WebElement titleInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[name='subject']"))
            );
            titleInput.clear();
            titleInput.sendKeys(productData.getName());
            
            // 내용 입력
            WebElement contentTextarea = webDriver.findElement(By.cssSelector("textarea[name='content']"));
            contentTextarea.clear();
            contentTextarea.sendKeys(productData.getDescription() + "\n\n가격: " + productData.getPrice() + "원");
            
            // 위치 입력
            if (productData.getLocation() != null) {
                WebElement locationInput = webDriver.findElement(By.cssSelector("input[name='location']"));
                locationInput.clear();
                locationInput.sendKeys(productData.getLocation());
            }
            
            return true;
        } catch (Exception e) {
            log.error("중고나라 상품 폼 입력 실패: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 중고나라 상품 등록
     */
    private boolean submitJunggonaraProduct(WebDriver webDriver) {
        try {
            WebElement submitButton = webDriver.findElement(By.cssSelector("button[type='submit']"));
            submitButton.click();
            
            // 등록 완료까지 대기
            WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(30));
            wait.until(ExpectedConditions.urlContains("ArticleRead.nhn"));
            
            return true;
        } catch (Exception e) {
            log.error("중고나라 상품 등록 실패: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 중고나라 등록 성공 확인
     */
    private String verifyJunggonaraRegistration(WebDriver webDriver) {
        try {
            String currentUrl = webDriver.getCurrentUrl();
            if (currentUrl.contains("ArticleRead.nhn")) {
                return currentUrl;
            }
            return null;
        } catch (Exception e) {
            log.error("중고나라 등록 성공 확인 실패: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 스크린샷 촬영
     */
    private String takeScreenshot(WebDriver webDriver, String fileNamePrefix) {
        try {
            if (webDriver instanceof TakesScreenshot) {
                TakesScreenshot screenshot = (TakesScreenshot) webDriver;
                byte[] screenshotBytes = screenshot.getScreenshotAs(OutputType.BYTES);
                String fileName = fileNamePrefix + "_" + System.currentTimeMillis() + ".png";
                // 실제 파일 저장 로직은 필요에 따라 구현
                log.info("스크린샷 촬영: {}", fileName);
                return fileName;
            }
        } catch (Exception e) {
            log.error("스크린샷 촬영 실패: {}", e.getMessage());
        }
        return null;
    }
}
