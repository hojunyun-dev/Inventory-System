package com.inventory.registration.service;

import com.inventory.registration.constants.PlatformConstants;
import com.inventory.registration.constants.SelectorConstants;
import com.inventory.registration.model.AutomationResult;
import com.inventory.registration.model.ProductData;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
@Slf4j
public class JunggonaraAutomationService extends BaseAutomationService {
    
    @Override
    protected String getPlatformName() {
        return PlatformConstants.JUNGGONARA;
    }
    
    @Override
    protected String getLoginUrl() {
        return PlatformConstants.JUNGGONARA_LOGIN_URL;
    }
    
    @Override
    protected String getRegisterUrl() {
        return PlatformConstants.JUNGGONARA_REGISTER_URL;
    }
    
    @Override
    protected boolean fillLoginForm(String username, String password) {
        try {
            log.info("Filling login form for Junggonara");
            
            // 아이디 입력
            WebElement idInput = wait.until(webDriver -> 
                webDriver.findElement(By.cssSelector(SelectorConstants.Junggonara.LOGIN_ID_INPUT)));
            safeInput(idInput, username);
            
            // 비밀번호 입력
            WebElement passwordInput = webDriver.findElement(By.cssSelector(SelectorConstants.Junggonara.LOGIN_PASSWORD_INPUT));
            safeInput(passwordInput, password);
            
            // 로그인 버튼 클릭
            WebElement loginButton = webDriver.findElement(By.cssSelector(SelectorConstants.Junggonara.LOGIN_BUTTON));
            safeClick(loginButton);
            
            return true;
        } catch (Exception e) {
            log.error("Failed to fill login form: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    protected boolean waitForLoginSuccess() {
        try {
            // 로그인 성공 확인
            wait.until(webDriver -> 
                webDriver.findElement(By.cssSelector(SelectorConstants.Junggonara.LOGIN_SUCCESS_INDICATOR)));
            log.info("Junggonara login successful");
            return true;
        } catch (Exception e) {
            log.error("Login success verification failed: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    protected boolean fillProductForm(ProductData productData) {
        try {
            log.info("Filling product form for Junggonara");
            
            // 제목 입력
            WebElement subjectInput = wait.until(webDriver -> 
                webDriver.findElement(By.cssSelector(SelectorConstants.Junggonara.PRODUCT_SUBJECT_INPUT)));
            safeInput(subjectInput, productData.getName());
            
            // 가격 입력
            if (productData.getPrice() != null) {
                try {
                    WebElement priceInput = webDriver.findElement(By.cssSelector(SelectorConstants.Junggonara.PRODUCT_PRICE_INPUT));
                    safeInput(priceInput, productData.getPrice().toString());
                } catch (Exception e) {
                    log.warn("Failed to input price: {}", e.getMessage());
                }
            }
            
            // 내용 입력
            WebElement contentTextarea = webDriver.findElement(By.cssSelector(SelectorConstants.Junggonara.PRODUCT_CONTENT_TEXTAREA));
            safeInput(contentTextarea, productData.getDescription());
            
            // 위치 입력
            if (productData.getLocation() != null) {
                try {
                    WebElement locationInput = webDriver.findElement(By.cssSelector(SelectorConstants.Junggonara.PRODUCT_LOCATION_INPUT));
                    safeInput(locationInput, productData.getLocation());
                } catch (Exception e) {
                    log.warn("Failed to input location: {}", e.getMessage());
                }
            }
            
            // 카테고리 선택
            if (productData.getCategory() != null) {
                try {
                    WebElement categorySelect = webDriver.findElement(By.cssSelector(SelectorConstants.Junggonara.PRODUCT_CATEGORY_SELECT));
                    Select categoryDropdown = new Select(categorySelect);
                    categoryDropdown.selectByVisibleText(productData.getCategory());
                } catch (Exception e) {
                    log.warn("Failed to select category: {}", e.getMessage());
                }
            }
            
            // 이미지 업로드
            if (productData.getImagePaths() != null && !productData.getImagePaths().isEmpty()) {
                uploadImages(productData.getImagePaths());
            }
            
            return true;
        } catch (Exception e) {
            log.error("Failed to fill product form: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    protected boolean submitProduct() {
        try {
            log.info("Submitting product for Junggonara");
            
            // 제출 버튼 클릭
            WebElement submitButton = webDriver.findElement(By.cssSelector(SelectorConstants.Junggonara.SUBMIT_BUTTON));
            safeClick(submitButton);
            
            return true;
        } catch (Exception e) {
            log.error("Failed to submit product: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    protected String waitForRegistrationSuccess() {
        try {
            // 성공 메시지 확인
            wait.until(webDriver -> 
                webDriver.findElement(By.cssSelector(SelectorConstants.Junggonara.SUCCESS_MESSAGE)));
            
            // 현재 URL에서 상품 정보 추출
            String currentUrl = webDriver.getCurrentUrl();
            log.info("Product registered successfully. URL: {}", currentUrl);
            
            return currentUrl;
        } catch (Exception e) {
            log.error("Failed to verify registration success: {}", e.getMessage());
            return null;
        }
    }
    
    private void uploadImages(List<String> imagePaths) {
        try {
            WebElement fileInput = webDriver.findElement(By.cssSelector(SelectorConstants.Junggonara.PRODUCT_IMAGE_UPLOAD));
            
            for (String imagePath : imagePaths) {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    fileInput.sendKeys(imageFile.getAbsolutePath());
                    log.info("Uploaded image: {}", imagePath);
                } else {
                    log.warn("Image file not found: {}", imagePath);
                }
            }
        } catch (Exception e) {
            log.error("Failed to upload images: {}", e.getMessage());
        }
    }
    
    // 공개 메서드 - 외부에서 호출
    public AutomationResult registerProduct(ProductData productData, String username, String password) {
        try {
            // 로그인
            if (!login(username, password)) {
                return AutomationResult.builder()
                        .platform(getPlatformName())
                        .success(false)
                        .errorMessage("Login failed")
                        .build();
            }
            
            // 상품 등록
            return registerProduct(productData);
            
        } catch (Exception e) {
            log.error("Junggonara automation failed: {}", e.getMessage());
            return AutomationResult.builder()
                    .platform(getPlatformName())
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }
}




