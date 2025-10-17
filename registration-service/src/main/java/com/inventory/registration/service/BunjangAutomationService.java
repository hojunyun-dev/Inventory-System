package com.inventory.registration.service;

import com.inventory.registration.constants.PlatformConstants;
import com.inventory.registration.constants.SelectorConstants;
import com.inventory.registration.model.AutomationResult;
import com.inventory.registration.model.ProductData;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
@Slf4j
public class BunjangAutomationService extends BaseAutomationService {
    
    // 카테고리 매핑 맵
    private static final Map<String, String> CATEGORY_MAPPING = new HashMap<>();
    static {
        CATEGORY_MAPPING.put("디지털", "//button[contains(text(), '디지털')]");
        CATEGORY_MAPPING.put("여성의류", "//button[contains(text(), '여성의류')]");
        CATEGORY_MAPPING.put("남성의류", "//button[contains(text(), '남성의류')]");
        CATEGORY_MAPPING.put("신발", "//button[contains(text(), '신발')]");
        CATEGORY_MAPPING.put("가방/지갑", "//button[contains(text(), '가방/지갑')]");
        CATEGORY_MAPPING.put("시계", "//button[contains(text(), '시계')]");
        CATEGORY_MAPPING.put("쥬얼리", "//button[contains(text(), '쥬얼리')]");
        CATEGORY_MAPPING.put("패션 액세서리", "//button[contains(text(), '패션 액세서리')]");
        CATEGORY_MAPPING.put("가전제품", "//button[contains(text(), '가전제품')]");
        CATEGORY_MAPPING.put("스포츠/레저", "//button[contains(text(), '스포츠/레저')]");
        CATEGORY_MAPPING.put("차량/오토바이", "//button[contains(text(), '차량/오토바이')]");
        CATEGORY_MAPPING.put("스타굿즈", "//button[contains(text(), '스타굿즈')]");
        CATEGORY_MAPPING.put("키덜트", "//button[contains(text(), '키덜트')]");
        CATEGORY_MAPPING.put("예술/희귀/수집품", "//button[contains(text(), '예술/희귀/수집품')]");
        CATEGORY_MAPPING.put("음반/악기", "//button[contains(text(), '음반/악기')]");
        CATEGORY_MAPPING.put("도서/티켓/문구", "//button[contains(text(), '도서/티켓/문구')]");
        CATEGORY_MAPPING.put("뷰티/미용", "//button[contains(text(), '뷰티/미용')]");
        CATEGORY_MAPPING.put("가구/인테리어", "//button[contains(text(), '가구/인테리어')]");
        CATEGORY_MAPPING.put("생활/주방용품", "//button[contains(text(), '생활/주방용품')]");
        CATEGORY_MAPPING.put("공구/산업용품", "//button[contains(text(), '공구/산업용품')]");
        CATEGORY_MAPPING.put("식품", "//button[contains(text(), '식품')]");
        CATEGORY_MAPPING.put("유아동/출산", "//button[contains(text(), '유아동/출산')]");
        CATEGORY_MAPPING.put("반려동물용품", "//button[contains(text(), '반려동물용품')]");
        CATEGORY_MAPPING.put("기타", "//button[contains(text(), '기타')]");
        CATEGORY_MAPPING.put("재능", "//button[contains(text(), '재능')]");
    }
    
    private String getCategoryButtonXPath(String category) {
        return CATEGORY_MAPPING.getOrDefault(category, "//button[contains(text(), '기타')]");
    }
    
    // 공개 메서드 - 컨트롤러에서 호출 가능
    public AutomationResult registerProductPublic(ProductData productData) {
        return registerProduct(productData);
    }
    
    // 로그인 테스트 전용 메서드
    public boolean testLogin(String username, String password) {
        try {
            log.info("번개장터 로그인 테스트 시작 - 사용자: {}", username);
            
            // 로그인 시도
            boolean loginResult = login(username, password);
            
            if (loginResult) {
                log.info("번개장터 로그인 테스트 성공!");
                return true;
            } else {
                log.warn("번개장터 로그인 테스트 실패");
                return false;
            }
            
        } catch (Exception e) {
            log.error("번개장터 로그인 테스트 중 예외 발생: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    protected String getPlatformName() {
        return PlatformConstants.BUNJANG;
    }
    
    @Override
    protected String getLoginUrl() {
        return PlatformConstants.BUNJANG_LOGIN_URL;
    }
    
    @Override
    protected String getRegisterUrl() {
        return PlatformConstants.BUNJANG_REGISTER_URL;
    }
    
    @Override
    protected boolean fillLoginForm(String username, String password) {
        try {
            log.info("Filling Naver OAuth login form for Bunjang");
            
            // 번개장터 로그인 페이지로 이동
            webDriver.get(getLoginUrl());
            
            // 네이버 로그인 버튼 클릭
            WebElement naverLoginButton = wait.until(webDriver -> 
                webDriver.findElement(By.xpath("//button[contains(text(), '네이버로 이용하기')]")));
            safeClick(naverLoginButton);
            
            // 새 탭으로 네이버 로그인 페이지가 열림 - 새 탭으로 전환
            String originalWindow = webDriver.getWindowHandle();
            for (String windowHandle : webDriver.getWindowHandles()) {
                if (!windowHandle.equals(originalWindow)) {
                    webDriver.switchTo().window(windowHandle);
                    break;
                }
            }
            
                    // 네이버 로그인 페이지에서 아이디/비밀번호 입력
                    WebElement idInput = wait.until(webDriver -> 
                        webDriver.findElement(By.cssSelector(SelectorConstants.Bunjang.NAVER_ID_INPUT)));
                    safeInput(idInput, username);
                    
                    WebElement pwInput = webDriver.findElement(By.cssSelector(SelectorConstants.Bunjang.NAVER_PASSWORD_INPUT));
                    safeInput(pwInput, password);
                    
                    WebElement naverLoginSubmit = webDriver.findElement(By.cssSelector(SelectorConstants.Bunjang.NAVER_LOGIN_SUBMIT));
                    safeClick(naverLoginSubmit);
            
            // 번개장터로 리다이렉션 대기
            wait.until(webDriver -> webDriver.getCurrentUrl().contains("bunjang.co.kr"));
            
            // 원래 탭으로 돌아가기
            webDriver.switchTo().window(originalWindow);
            
            return true;
        } catch (Exception e) {
            log.error("Failed to fill Naver OAuth login form: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    protected boolean waitForLoginSuccess() {
        try {
            // 로그인 성공 확인 (사용자 정보가 표시되는지 확인)
            wait.until(webDriver -> 
                webDriver.findElement(By.cssSelector(SelectorConstants.Bunjang.LOGIN_SUCCESS_INDICATOR)));
            log.info("Bunjang login successful");
            return true;
        } catch (Exception e) {
            log.error("Login success verification failed: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    protected boolean fillProductForm(ProductData productData) {
        try {
            log.info("Filling product form for Bunjang");
            
                    // 상품명 입력
                    WebElement nameInput = wait.until(webDriver -> 
                        webDriver.findElement(By.cssSelector(SelectorConstants.Bunjang.PRODUCT_NAME_INPUT)));
                    safeInput(nameInput, productData.getName());
                    
                    // 가격 입력
                    WebElement priceInput = webDriver.findElement(By.cssSelector(SelectorConstants.Bunjang.PRODUCT_PRICE_INPUT));
                    safeInput(priceInput, productData.getPrice().toString());
                    
                    // 상품 설명 입력
                    WebElement descriptionTextarea = webDriver.findElement(By.cssSelector(SelectorConstants.Bunjang.PRODUCT_DESCRIPTION_TEXTAREA));
                    safeInput(descriptionTextarea, productData.getDescription());
            
            // 카테고리 선택 (실제 번개장터 구조에 맞게)
            if (productData.getCategory() != null) {
                try {
                    String categoryButtonXPath = getCategoryButtonXPath(productData.getCategory());
                    WebElement categoryButton = webDriver.findElement(By.xpath(categoryButtonXPath));
                    safeClick(categoryButton);
                    log.info("Selected category: {}", productData.getCategory());
                } catch (Exception e) {
                    log.warn("Failed to select category '{}': {}", productData.getCategory(), e.getMessage());
                    // 기본값으로 '기타' 카테고리 선택
                    try {
                        WebElement etcCategory = webDriver.findElement(By.xpath("//button[contains(text(), '기타')]"));
                        safeClick(etcCategory);
                        log.info("Selected default category: 기타");
                    } catch (Exception e2) {
                        log.warn("Failed to select default category: {}", e2.getMessage());
                    }
                }
            }
            
            // 상품 상태 선택 (번개장터는 기본적으로 새상품/중고 선택이 간단함)
            if (productData.getCondition() != null) {
                try {
                    // 상품 상태는 번개장터에서 자동으로 처리되므로 로그만 남김
                    log.info("Product condition: {}", productData.getCondition());
                } catch (Exception e) {
                    log.warn("Failed to set condition: {}", e.getMessage());
                }
            }
            
            // 위치 정보 (번개장터는 GPS 기반이므로 수동 입력 불가)
            if (productData.getLocation() != null) {
                try {
                    log.info("Product location: {} (번개장터는 GPS 기반 위치 사용)", productData.getLocation());
                } catch (Exception e) {
                    log.warn("Failed to set location: {}", e.getMessage());
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
            log.info("Submitting product for Bunjang");
            
            // 제출 버튼 클릭
            WebElement submitButton = webDriver.findElement(By.xpath("//button[contains(text(), '등록하기')]"));
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
                webDriver.findElement(By.cssSelector(SelectorConstants.Bunjang.SUCCESS_MESSAGE)));
            
            // 현재 URL에서 상품 ID 추출
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
            WebElement fileInput = webDriver.findElement(By.cssSelector(SelectorConstants.Bunjang.PRODUCT_IMAGE_UPLOAD));
            
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
            log.error("Bunjang automation failed: {}", e.getMessage());
            return AutomationResult.builder()
                    .platform(getPlatformName())
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }
}
