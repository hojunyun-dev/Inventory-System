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
public class DanggeunAutomationService extends BaseAutomationService {
    
    @Override
    protected String getPlatformName() {
        return PlatformConstants.DANGGEUN;
    }
    
    @Override
    protected String getLoginUrl() {
        return PlatformConstants.DANGGEUN_LOGIN_URL;
    }
    
    @Override
    protected String getRegisterUrl() {
        return PlatformConstants.DANGGEUN_REGISTER_URL;
    }
    
    @Override
    protected boolean fillLoginForm(String username, String password) {
        try {
            log.info("Filling login form for Danggeun");
            
            // 당근마켓은 휴대폰 번호로 로그인
            WebElement phoneInput = wait.until(webDriver -> 
                webDriver.findElement(By.cssSelector(SelectorConstants.Danggeun.LOGIN_PHONE_INPUT)));
            safeInput(phoneInput, username);
            
            // 인증번호 입력 (실제로는 SMS 인증이 필요하므로 수동 처리 필요)
            try {
                WebElement verificationInput = webDriver.findElement(By.cssSelector(SelectorConstants.Danggeun.LOGIN_VERIFICATION_INPUT));
                // 인증번호는 사용자가 수동으로 입력해야 함
                log.warn("SMS verification required. Please input verification code manually.");
                Thread.sleep(30000); // 30초 대기 (사용자가 인증번호 입력할 시간)
            } catch (Exception e) {
                log.warn("Verification input not found, proceeding without verification");
            }
            
            // 로그인 버튼 클릭
            WebElement loginButton = webDriver.findElement(By.cssSelector(SelectorConstants.Danggeun.LOGIN_BUTTON));
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
                webDriver.findElement(By.cssSelector(SelectorConstants.Danggeun.LOGIN_SUCCESS_INDICATOR)));
            log.info("Danggeun login successful");
            return true;
        } catch (Exception e) {
            log.error("Login success verification failed: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    protected boolean fillProductForm(ProductData productData) {
        try {
            log.info("Filling product form for Danggeun");
            
            // 상품 제목 입력
            WebElement titleInput = wait.until(webDriver -> 
                webDriver.findElement(By.cssSelector(SelectorConstants.Danggeun.PRODUCT_TITLE_INPUT)));
            safeInput(titleInput, productData.getName());
            
            // 가격 입력
            WebElement priceInput = webDriver.findElement(By.cssSelector(SelectorConstants.Danggeun.PRODUCT_PRICE_INPUT));
            safeInput(priceInput, productData.getPrice().toString());
            
            // 상품 내용 입력
            WebElement contentTextarea = webDriver.findElement(By.cssSelector(SelectorConstants.Danggeun.PRODUCT_CONTENT_TEXTAREA));
            safeInput(contentTextarea, productData.getDescription());
            
            // 카테고리 선택
            if (productData.getCategory() != null) {
                try {
                    WebElement categorySelect = webDriver.findElement(By.cssSelector(SelectorConstants.Danggeun.PRODUCT_CATEGORY_SELECT));
                    Select categoryDropdown = new Select(categorySelect);
                    categoryDropdown.selectByVisibleText(productData.getCategory());
                } catch (Exception e) {
                    log.warn("Failed to select category: {}", e.getMessage());
                }
            }
            
            // 위치 선택
            if (productData.getLocation() != null) {
                try {
                    WebElement locationSelect = webDriver.findElement(By.cssSelector(SelectorConstants.Danggeun.PRODUCT_LOCATION_SELECT));
                    Select locationDropdown = new Select(locationSelect);
                    locationDropdown.selectByVisibleText(productData.getLocation());
                } catch (Exception e) {
                    log.warn("Failed to select location: {}", e.getMessage());
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
            log.info("Submitting product for Danggeun");
            
            // 제출 버튼 클릭
            WebElement submitButton = webDriver.findElement(By.cssSelector(SelectorConstants.Danggeun.SUBMIT_BUTTON));
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
                webDriver.findElement(By.cssSelector(SelectorConstants.Danggeun.SUCCESS_MESSAGE)));
            
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
            WebElement fileInput = webDriver.findElement(By.cssSelector(SelectorConstants.Danggeun.PRODUCT_IMAGE_UPLOAD));
            
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
    public AutomationResult registerProduct(ProductData productData, String phoneNumber, String verificationCode) {
        try {
            // 로그인 (휴대폰 번호 + 인증번호)
            if (!login(phoneNumber, verificationCode)) {
                return AutomationResult.builder()
                        .platform(getPlatformName())
                        .success(false)
                        .errorMessage("Login failed - SMS verification may be required")
                        .build();
            }
            
            // 상품 등록
            return registerProduct(productData);
            
        } catch (Exception e) {
            log.error("Danggeun automation failed: {}", e.getMessage());
            return AutomationResult.builder()
                    .platform(getPlatformName())
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }
}




