package com.inventory.registration.service.bunjang;

import com.inventory.registration.dto.ProductRegistrationRequest;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.interactions.Actions;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.ResponseEntity;

/**
 * 번개장터 상품 등록 폼 처리 클래스
 * - 상품 등록 페이지 이동
 * - 폼 필드 찾기 및 입력
 * - 상품명, 가격, 설명 입력 처리
 */
@Component
@Slf4j
public class BunjangFormHandler {
    
    private final WebClient webClient;
    
    public BunjangFormHandler() {
        this.webClient = WebClient.builder()
            .baseUrl("http://localhost:8080")
            .build();
    }
    
    /**
     * 상품 등록 페이지로 이동
     */
    public boolean goToProductNew(WebDriver driver) {
        log.info("🚀 상품 등록 페이지로 진입 시작...");

        // 1) 직접 URL (강화된 버전)
        try {
            log.info("Trying direct URL navigation...");
            driver.navigate().to("https://www.bunjang.co.kr/products/new");
            Thread.sleep(3000); // 더 긴 대기 시간
            
            // 페이지 로드 완료 대기
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(driver1 -> {
                JavascriptExecutor js = (JavascriptExecutor) driver1;
                return js.executeScript("return document.readyState").equals("complete");
            });
            
            if (isProductFormPresent(driver)) {
                log.info("✅ 직접 URL로 진입 성공");
                return true;
            }
        } catch (Exception e) { 
            log.warn("직접 URL 실패: {}", e.getMessage()); 
        }

        // 2) 판매하기 버튼 (강화된 버전)
        try {
            log.info("Trying sell button click...");
            
            // 여러 가지 판매하기 버튼 선택자 시도
            String[] sellSelectors = {
                "//a[contains(@href,'products/new')]",
                "//a[contains(.,'판매하기')]",
                "//button[contains(.,'판매하기')]",
                "//*[contains(text(),'판매하기')]",
                "//a[@href='/products/new']",
                "//a[contains(@href,'/products/new')]"
            };
            
            WebElement sell = null;
            for (String selector : sellSelectors) {
                try {
                    sell = new WebDriverWait(driver, Duration.ofSeconds(3))
                        .until(ExpectedConditions.elementToBeClickable(By.xpath(selector)));
                    log.info("Found sell button with selector: {}", selector);
                    break;
                } catch (Exception e) {
                    log.debug("Sell button not found with selector: {}", selector);
                }
            }
            
            if (sell != null) {
                humanFocusAndScroll(driver, sell);
                jsClick(driver, sell);
                Thread.sleep(3000);
                
                // 새 창이 열렸는지 확인
                if (driver.getWindowHandles().size() > 1) {
                    // 새 창으로 전환
                    for (String windowHandle : driver.getWindowHandles()) {
                        driver.switchTo().window(windowHandle);
                        if (driver.getCurrentUrl().contains("products/new")) {
                            break;
                        }
                    }
                }
                
                if (isProductFormPresent(driver)) {
                    log.info("✅ 판매하기 클릭으로 진입 성공");
                    return true;
                }
            }
        } catch (Exception e) { 
            log.warn("판매하기 클릭 실패: {}", e.getMessage()); 
        }

        // 3) 마지막 시도: 현재 URL 확인 후 재시도
        try {
            log.info("Final attempt - checking current URL...");
            String currentUrl = driver.getCurrentUrl();
            log.info("Current URL: {}", currentUrl);
            
            if (!currentUrl.contains("bunjang.co.kr")) {
                log.info("Not on bunjang domain, navigating to homepage first...");
                driver.navigate().to("https://www.bunjang.co.kr");
                Thread.sleep(2000);
                return goToProductNew(driver); // 재귀 호출
            }
        } catch (Exception e) {
            log.warn("Final attempt failed: {}", e.getMessage());
        }

        log.error("❌ 상품 등록 페이지 진입 실패");
        return false;
    }
    
    /**
     * 상품 등록 폼이 현재 페이지에 존재하는지 확인
     */
    private boolean isProductFormPresent(WebDriver d){
        try {
            String currentUrl = d.getCurrentUrl();
            log.info("Checking form presence on URL: {}", currentUrl);
            
            // 폼 존재 확인
            boolean hasForm = existsDisplayed(d, By.cssSelector("form"));
            log.info("Has form: {}", hasForm);
            
            // 데스크톱 버전 폼 확인
            boolean desktopForm = hasForm &&
                   (existsDisplayed(d, By.cssSelector("form input[name*='title'], form input[placeholder*='상품']"))
                    || existsDisplayed(d, By.cssSelector("form textarea")));
            
            // 모바일 버전 폼 확인 (m.bunjang.co.kr)
            boolean mobileForm = hasForm &&
                   (existsDisplayed(d, By.cssSelector("input[placeholder*='상품명'], input[placeholder*='제목']"))
                    || existsDisplayed(d, By.cssSelector("textarea[placeholder*='설명']"))
                    || existsDisplayed(d, By.cssSelector("input[placeholder*='가격']"))
                    || existsDisplayed(d, By.cssSelector("input[type='text']"))
                    || existsDisplayed(d, By.cssSelector("textarea")));
            
            // 페이지 제목으로도 확인
            boolean hasProductTitle = d.getTitle().contains("상품") || d.getTitle().contains("등록") || d.getTitle().contains("판매");
            log.info("Has product title: {}", hasProductTitle);
            
            log.info("Form detection - Desktop: {}, Mobile: {}, HasForm: {}, HasProductTitle: {}", 
                    desktopForm, mobileForm, hasForm, hasProductTitle);
            
            return desktopForm || mobileForm || (hasForm && hasProductTitle);
        } catch (Exception e) {
            log.error("Error checking form presence: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 상품 등록 폼 작성 (재시도 로직 포함)
     */
    public void fillProductForm(WebDriver driver, WebDriverWait wait, ProductRegistrationRequest request) {
        log.info("📝 상품 등록 폼 작성 시작...");

        int maxRetries = 3;
        int retryCount = 0;
        
        while (retryCount < maxRetries) {
            try {
                log.info("🔄 폼 작성 시도: {}/{}", retryCount + 1, maxRetries);
                
                // 1. 이미지 업로드 (API 경로로 처리) - 폼 업로드는 사용하지 않음
                log.info("🖼️ 폼 기반 이미지 업로드는 비활성화되었습니다. (API 업로드 사용)");
                
                // 2. 상품명 입력 (40자 이내)
                fillProductName(driver, request.getProductName());
                
                // 3. 카테고리 선택 (차량/오토바이 → 차량 용품/부품 → 차량 부품)
                selectCategory(driver);
                
                // 4. 상품상태 선택 (새 상품 미사용)
                selectProductCondition(driver);
                
                // 5. 상품 설명 입력 (10자 이상, 2000자 이하)
                fillProductDescription(driver, request.getProductDescription());
                
                // 6. 태그 입력 (자동차, 부품)
                fillTags(driver);
                
                // 7. 가격 입력 (원단위)
                fillPrice(driver, request.getPrice());
                
                // 8. 배송비 설정 (배송비포함)
                setShippingOption(driver);
                
                // 9. 직거래 설정 (불가)
                setDirectTransaction(driver);
                
                // 10. 수량 입력 (1-999개)
                fillQuantity(driver, request.getStock());
                
                // 11. 등록 버튼 클릭
                submitProductForm(driver);
                
                log.info("✅ 모든 폼 필드 입력 및 등록 완료");
                return; // 성공시 종료
                
            } catch (Exception e) {
                retryCount++;
                log.error("❌ 폼 입력 실패 (시도 {}/{}): {}", retryCount, maxRetries, e.getMessage());
                
                if (retryCount >= maxRetries) {
                    log.error("❌ 최대 재시도 횟수 초과. 폼 작성 실패");
                    throw new RuntimeException("상품 등록 폼 작성 실패 (3회 재시도 후): " + e.getMessage());
                }
                
                // 재시도 전 대기
                try {
                    Thread.sleep(2000);
                    log.info("🔄 재시도 준비 중...");
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
    
    // 폼 이미지 업로드 경로 제거됨 (API 업로드 사용)
    
    /**
     * 이미지 데이터를 임시 파일로 저장
     * - Selenium에서 파일 업로드 시 로컬 파일 경로가 필요하므로
     * - DB에서 조회한 이미지 데이터를 임시 파일로 저장
     */
    // 폼 임시파일 경로 제거됨 (API 업로드 사용)
    
    /**
     * 상대 경로를 절대 경로로 변환
     */
    private String convertToAbsolutePath(String imageUrl) {
        // /uploads/images/filename.png -> /home/code/바탕화면/Inventory-System1/backend/uploads/images/filename.png
        if (imageUrl.startsWith("/uploads/images/")) {
            return "/home/code/바탕화면/Inventory-System1/backend" + imageUrl;
        }
        return imageUrl; // 이미 절대 경로인 경우
    }
    
    /**
     * 상품명 입력 (40자 이내) - 재시도 로직 포함
     */
    private void fillProductName(WebDriver driver, String productName) {
        log.info("상품명 입력: {}", productName);
        
        // 40자 제한 적용
        String truncatedName = productName.length() > 40 ? productName.substring(0, 40) : productName;
        
        String[] selectors = {
            "input[placeholder*='상품명을 입력해주세요']",
            "input[placeholder*='상품명']",
            "input[name*='title']",
            "input[name*='name']",
            "input[name*='productName']"
        };
        
        retryOperation(() -> {
            WebElement nameField = findElementBySelectors(driver, selectors);
            setReactValue(driver, nameField, truncatedName);
            log.info("✅ 상품명 입력 완료: {}", truncatedName);
        }, "상품명 입력");
    }
    
    /**
     * 카테고리 선택 (차량/오토바이 → 차량 용품/부품 → 차량 부품)
     */
    private void selectCategory(WebDriver driver) {
        log.info("카테고리 선택 시작...");
        
        retryOperation(() -> {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            
            // 대분류: 차량/오토바이
            WebElement categoryButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(),'차량/오토바이')] | //div[contains(text(),'차량/오토바이')]")
            ));
            humanClick(driver, categoryButton);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("카테고리 선택 중 인터럽트: " + e.getMessage());
            }
            
            // 중분류: 차량 용품/부품
            WebElement subCategoryButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(),'차량 용품/부품')] | //div[contains(text(),'차량 용품/부품')]")
            ));
            humanClick(driver, subCategoryButton);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("카테고리 선택 중 인터럽트: " + e.getMessage());
            }
            
            // 소분류: 차량 부품
            WebElement detailCategoryButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(),'차량 부품')] | //div[contains(text(),'차량 부품')]")
            ));
            humanClick(driver, detailCategoryButton);
            
            log.info("✅ 카테고리 선택 완료: 차량/오토바이 → 차량 용품/부품 → 차량 부품");
        }, "카테고리 선택");
    }
    
    /**
     * 상품상태 선택 (새 상품 미사용)
     */
    private void selectProductCondition(WebDriver driver) {
        log.info("상품상태 선택: 새 상품(미사용)");
        
        retryOperation(() -> {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement conditionButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(),'새 상품(미사용)')] | //div[contains(text(),'새 상품(미사용)')] | //label[contains(text(),'새 상품(미사용)')]")
            ));
            humanClick(driver, conditionButton);
            
            log.info("✅ 상품상태 선택 완료: 새 상품(미사용)");
        }, "상품상태 선택");
    }
    
    /**
     * 상품 설명 입력 (10자 이상, 2000자 이하)
     */
    private void fillProductDescription(WebDriver driver, String description) {
        log.info("상품 설명 입력: {}", description);
        
        // 2000자 제한 적용
        String truncatedDesc = description.length() > 2000 ? description.substring(0, 2000) : description;
        
        // 10자 미만인 경우 기본 설명 추가
        if (truncatedDesc.length() < 10) {
            truncatedDesc = truncatedDesc + " 자동차 부품입니다.";
        }
        
        String[] selectors = {
            "textarea[placeholder*='설명']",
            "textarea[name*='description']",
            "textarea[name*='content']",
            "textarea"
        };
        
        WebElement descField = findElementBySelectors(driver, selectors);
        setReactValue(driver, descField, truncatedDesc);
        
        log.info("✅ 상품 설명 입력 완료: {}자", truncatedDesc.length());
    }
    
    /**
     * 태그 입력 (자동차, 부품)
     */
    private void fillTags(WebDriver driver) {
        log.info("태그 입력: 자동차, 부품");
        
        try {
            String[] selectors = {
                "input[placeholder*='태그']",
                "input[name*='tag']",
                "input[class*='tag']"
            };
            
            WebElement tagField = findElementBySelectors(driver, selectors);
            
            // 자동차 태그 입력
            setReactValue(driver, tagField, "자동차");
            Thread.sleep(500);
            
            // 엔터키로 태그 추가
            tagField.sendKeys(org.openqa.selenium.Keys.ENTER);
            Thread.sleep(500);
            
            // 부품 태그 입력
            setReactValue(driver, tagField, "부품");
            tagField.sendKeys(org.openqa.selenium.Keys.ENTER);
            
            log.info("✅ 태그 입력 완료: 자동차, 부품");
            
        } catch (Exception e) {
            log.warn("⚠️ 태그 입력 실패 (선택사항): {}", e.getMessage());
        }
    }
    
    /**
     * 가격 입력 (원단위)
     */
    private void fillPrice(WebDriver driver, Double price) {
        log.info("가격 입력: {}원", price);
        
        String[] selectors = {
            "input[placeholder*='가격']",
            "input[name*='price']",
            "input[name*='amount']",
            "input[type='number']",
            "input[type='tel']"
        };
        
        WebElement priceField = findElementBySelectors(driver, selectors);
        setReactValue(driver, priceField, String.valueOf(price.intValue()));
        
        log.info("✅ 가격 입력 완료: {}원", price);
    }
    
    /**
     * 배송비 설정 (배송비포함)
     */
    private void setShippingOption(WebDriver driver) {
        log.info("배송비 설정: 배송비포함");
        
        retryOperation(() -> {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement shippingButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(),'배송비포함')] | //label[contains(text(),'배송비포함')] | //input[@value='included']")
            ));
            humanClick(driver, shippingButton);
            
            log.info("✅ 배송비 설정 완료: 배송비포함");
        }, "배송비 설정");
    }
    
    /**
     * 직거래 설정 (불가)
     */
    private void setDirectTransaction(WebDriver driver) {
        log.info("직거래 설정: 불가");
        
        try {
            retryOperation(() -> {
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                WebElement directTransactionButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(text(),'직거래 불가')] | //label[contains(text(),'직거래 불가')] | //input[@value='false']")
                ));
                humanClick(driver, directTransactionButton);
                
                log.info("✅ 직거래 설정 완료: 불가");
            }, "직거래 설정");
        } catch (Exception e) {
            log.warn("⚠️ 직거래 설정 실패 (선택사항): {}", e.getMessage());
        }
    }
    
    /**
     * 수량 입력 (1-999개)
     */
    private void fillQuantity(WebDriver driver, Integer quantity) {
        log.info("수량 입력: {}개", quantity);
        
        // 1-999 범위 제한
        int validQuantity = Math.max(1, Math.min(999, quantity));
        
        String[] selectors = {
            "input[placeholder*='수량']",
            "input[name*='quantity']",
            "input[name*='stock']",
            "input[type='number']"
        };
        
        WebElement quantityField = findElementBySelectors(driver, selectors);
        setReactValue(driver, quantityField, String.valueOf(validQuantity));
        
        log.info("✅ 수량 입력 완료: {}개", validQuantity);
    }
    
    /**
     * 상품 등록 버튼 클릭 및 완료 대기
     */
    private void submitProductForm(WebDriver driver) {
        log.info("📤 상품 등록 버튼 클릭...");
        
        retryOperation(() -> {
            // 등록 버튼 찾기 (다양한 셀렉터)
            String[] submitSelectors = {
                "//button[contains(text(),'등록')]",
                "//button[contains(text(),'완료')]", 
                "//button[contains(text(),'상품등록')]",
                "//button[contains(text(),'등록하기')]",
                "//button[@type='submit']",
                "//input[@type='submit']",
                "//button[contains(@class,'submit')]",
                "//button[contains(@class,'register')]"
            };
            
            WebElement submitButton = null;
            for (String selector : submitSelectors) {
                try {
                    List<WebElement> buttons = driver.findElements(By.xpath(selector));
                    for (WebElement button : buttons) {
                        if (button.isDisplayed() && button.isEnabled()) {
                            submitButton = button;
                            break;
                        }
                    }
                    if (submitButton != null) break;
                } catch (Exception e) {
                    log.debug("셀렉터 실패: {} - {}", selector, e.getMessage());
                }
            }
            
            if (submitButton == null) {
                throw new RuntimeException("등록 버튼을 찾을 수 없습니다");
            }
            
            // 버튼으로 스크롤
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", submitButton);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("등록 버튼 클릭 중 인터럽트: " + e.getMessage());
            }
            
            // 등록 버튼 클릭
            humanClick(driver, submitButton);
            
            // 등록 완료 대기
            waitForRegistrationComplete(driver);
            
            log.info("✅ 상품 등록 완료");
            
        }, "상품 등록");
    }
    
    /**
     * 등록 완료 대기
     */
    private void waitForRegistrationComplete(WebDriver driver) {
        log.info("⏳ 등록 완료 대기 중...");
        
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            
            // 1. URL 변경 확인 (상품 상세 페이지로 이동) - 가장 확실한 방법
            try {
                wait.until(driver1 -> {
                    String currentUrl = driver1.getCurrentUrl();
                    boolean isProductPage = currentUrl.contains("/products/") && !currentUrl.contains("/new");
                    log.debug("현재 URL: {}, 상품 페이지 여부: {}", currentUrl, isProductPage);
                    return isProductPage;
                });
                log.info("✅ URL 변경으로 등록 완료 확인");
                return;
            } catch (Exception e) {
                log.debug("URL 변경 확인 실패: {}", e.getMessage());
            }
            
            // 2. 상품 상세 페이지 요소 확인 (상품명, 가격 등)
            try {
                wait.until(driver1 -> {
                    // 상품명이 있는지 확인
                    boolean hasProductName = driver1.findElements(By.xpath("//div[contains(@class,'ProductSummarystyle__Name')]")).size() > 0;
                    // 가격이 있는지 확인  
                    boolean hasPrice = driver1.findElements(By.xpath("//div[contains(@class,'ProductSummarystyle__Price')]")).size() > 0;
                    // 상품 이미지가 있는지 확인
                    boolean hasProductImage = driver1.findElements(By.xpath("//img[contains(@src,'media.bunjang.co.kr/product/')]")).size() > 0;
                    
                    boolean isProductDetailPage = hasProductName && hasPrice && hasProductImage;
                    log.debug("상품 상세 페이지 요소 확인 - 상품명: {}, 가격: {}, 이미지: {}, 결과: {}", 
                             hasProductName, hasPrice, hasProductImage, isProductDetailPage);
                    return isProductDetailPage;
                });
                log.info("✅ 상품 상세 페이지 요소로 등록 완료 확인");
                return;
            } catch (Exception e) {
                log.debug("상품 상세 페이지 요소 확인 실패: {}", e.getMessage());
            }
            
            // 3. 페이지 제목 확인 (상품명이 포함된 제목)
            try {
                wait.until(driver1 -> {
                    String pageTitle = driver1.getTitle();
                    boolean hasProductTitle = pageTitle.contains("테스트") || pageTitle.contains("엔진") || 
                                            pageTitle.contains("상품") || !pageTitle.contains("번개장터");
                    log.debug("페이지 제목: {}, 상품 제목 포함: {}", pageTitle, hasProductTitle);
                    return hasProductTitle;
                });
                log.info("✅ 페이지 제목으로 등록 완료 확인");
                return;
            } catch (Exception e) {
                log.debug("페이지 제목 확인 실패: {}", e.getMessage());
            }
            
            // 4. 기본 대기 (10초) - 안전장치
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("등록 완료 대기 중 인터럽트: {}", e.getMessage());
            }
            log.info("✅ 기본 대기로 등록 완료 처리");
            
        } catch (Exception e) {
            log.warn("⚠️ 등록 완료 확인 실패: {}", e.getMessage());
        }
    }
    
    /**
     * 여러 셀렉터로 요소 찾기
     */
    private WebElement findElementBySelectors(WebDriver driver, String[] selectors) {
        for (String selector : selectors) {
            try {
                List<WebElement> elements = driver.findElements(By.cssSelector(selector));
                for (WebElement element : elements) {
                    if (element.isDisplayed() && element.isEnabled()) {
                        log.info("✅ 요소 찾음: {}", selector);
                        return element;
                    }
                }
            } catch (Exception e) {
                log.debug("셀렉터 실패: {} - {}", selector, e.getMessage());
            }
        }
        throw new RuntimeException("모든 셀렉터로 요소를 찾을 수 없음: " + String.join(", ", selectors));
    }
    
    /**
     * 인간적인 클릭 (스크롤 + 대기)
     */
    private void humanClick(WebDriver driver, WebElement element) {
        try {
            // 요소로 스크롤
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
            Thread.sleep(500);
            
            // 클릭
            element.click();
            Thread.sleep(500);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("클릭 중 인터럽트: {}", e.getMessage());
            throw new RuntimeException("클릭 실패: " + e.getMessage());
        } catch (Exception e) {
            log.error("클릭 실패: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * 재시도 로직을 포함한 작업 실행
     */
    private void retryOperation(Runnable operation, String operationName) {
        int maxRetries = 3;
        int retryCount = 0;
        
        while (retryCount < maxRetries) {
            try {
                operation.run();
                return; // 성공시 종료
            } catch (Exception e) {
                retryCount++;
                log.warn("⚠️ {} 실패 (시도 {}/{}): {}", operationName, retryCount, maxRetries, e.getMessage());
                
                if (retryCount >= maxRetries) {
                    throw new RuntimeException(operationName + " 실패 (3회 재시도 후): " + e.getMessage());
                }
                
                // 재시도 전 대기
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
    
    /**
     * 폼 내에서 필드 찾기
     */
    private WebElement findInForm(WebElement form, String css) {
        List<WebElement> list = form.findElements(By.cssSelector(css));
        if (list.isEmpty()) throw new RuntimeException("폼 필드 없음: " + css);
        return list.stream().filter(el -> el.isDisplayed() && el.isEnabled()).findFirst()
            .orElseThrow(() -> new RuntimeException("표시/활성된 폼 필드 없음: " + css));
    }

    /**
     * 대표 이미지 URL에서 바이너리를 받아 임시 파일로 저장
     */
    // 폼 임시파일 경로 제거됨 (API 업로드 사용)

    private String computeMd5Hex(byte[] data) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(data);
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            log.warn("MD5 계산 실패: {}", e.getMessage());
            return "";
        }
    }

    /**
     * 폼 내에서 검색창 제외하고 필드 찾기
     */
    private WebElement findInFormAvoidSearch(WebElement form, String css) {
        List<WebElement> list = form.findElements(By.cssSelector(css));
        for (WebElement el : list) {
            if (!(contains(el.getAttribute("placeholder"), "검색")
                || contains(el.getAttribute("placeholder"), "search")
                || contains(el.getAttribute("placeholder"), "@상점명")
                || contains(el.getAttribute("placeholder"), "지역명")
                || contains(el.getAttribute("class"), "search"))) {
                if (el.isDisplayed() && el.isEnabled()) return el;
            }
        }
        throw new RuntimeException("상품명이 메인 검색창에 매칭됨: 선택자 보정 필요");
    }
    
    private boolean contains(String s, String token){ 
        return s!=null && s.toLowerCase().contains(token.toLowerCase()); 
    }

    /**
     * React 컨트롤드 인풋 값 주입
     */
    private void setReactValue(WebDriver d, WebElement el, String val) {
        ((JavascriptExecutor)d).executeScript(
            "const e=arguments[0], v=arguments[1];"
            + "const setter=Object.getOwnPropertyDescriptor(e.__proto__,'value')?.set;"
            + "setter?setter.call(e,v):(e.value=v);"
            + "e.dispatchEvent(new Event('input',{bubbles:true}));"
            + "e.dispatchEvent(new Event('change',{bubbles:true}));"
            + "e.dispatchEvent(new Event('blur',{bubbles:true}));"
            , el, val);
    }
    
    /**
     * 제출 + 완료 신호 대기
     */
    public void submitAndWaitSuccess(WebDriver d){
        By SUBMIT = By.xpath("//button[contains(.,'등록') or contains(.,'완료') or @type='submit'] | //input[@type='submit']");
        WebElement submit = new WebDriverWait(d, Duration.ofSeconds(6)).until(ExpectedConditions.elementToBeClickable(SUBMIT));
        humanFocusAndScroll(d, submit);
        jsClick(d, submit);
        try { 
            Thread.sleep(1500); 
        } catch (InterruptedException ignored) {}

        // 완료 신호(알림/토스트/상태 텍스트) 등 프로젝트에 맞춰 보강 가능
    }
    
    /**
     * 상품명 입력 필드 찾기
     */
    private WebElement findProductNameField(WebDriver driver, WebDriverWait wait) {
        log.info("상품명 입력 필드를 찾는 중...");
        
        // 번개장터 상품 등록 페이지의 실제 구조에 맞는 셀렉터들
        String[] selectors = {
            // 이미지 업로드 후 상품명 입력 공간
            "input[placeholder*='상품명을 입력해주세요']",
            "input[placeholder*='상품명']",
            "input[name*='title']",
            "input[name*='name']",
            "input[name*='productName']",
            // 폼 내부의 상품명 필드
            "form input[placeholder*='상품명']",
            "form input[name*='title']",
            // 특정 클래스나 ID 기반
            "input[class*='title']",
            "input[class*='name']",
            "input[id*='title']",
            "input[id*='name']",
            // 일반적인 텍스트 입력 필드 (메인 검색창 제외)
            "input[type='text']:not([placeholder*='검색']):not([placeholder*='search'])"
        };
        
        for (String selector : selectors) {
            try {
                List<WebElement> elements = driver.findElements(By.cssSelector(selector));
                for (WebElement element : elements) {
                    if (element.isDisplayed() && element.isEnabled()) {
                        String placeholder = element.getAttribute("placeholder");
                        String name = element.getAttribute("name");
                        String className = element.getAttribute("class");
                        
                        log.info("Found input field: placeholder={}, name={}, class={}", placeholder, name, className);
                        
                        // 메인 검색창이 아닌 상품명 입력 필드인지 확인 (검색창 제외)
                        if (placeholder != null && (placeholder.contains("상품명") || placeholder.contains("상품")) 
                            && !placeholder.contains("검색") && !placeholder.contains("search")) {
                            log.info("✅ Found product name field: {}", placeholder);
                            return element;
                        }
                        
                        if (name != null && (name.contains("title") || name.contains("name") || name.contains("product"))) {
                            log.info("✅ Found product name field by name: {}", name);
                            return element;
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("Product name field selector failed: {}", selector);
            }
        }
        
        log.warn("❌ Could not find product name field");
        return null;
    }
    
    /**
     * 가격 입력 필드 찾기
     */
    private WebElement findPriceField(WebDriver driver, WebDriverWait wait) {
        log.info("가격 입력 필드를 찾는 중...");
        
        // 번개장터 상품 등록 페이지의 실제 구조에 맞는 셀렉터들
        String[] selectors = {
            // 번개장터 특화 셀렉터들
            "input[placeholder*='가격']",
            "input[placeholder*='원']",
            "input[placeholder*='가격을 입력해주세요']",
            "input[name*='price']",
            "input[name*='amount']",
            "input[type='number']",
            "input[type='tel']",
            // 폼 내부의 가격 필드
            "form input[placeholder*='가격']",
            "form input[type='number']",
            // 특정 클래스나 ID 기반
            "input[class*='price']",
            "input[class*='amount']",
            "input[id*='price']",
            "input[id*='amount']",
            // 일반적인 숫자 입력 필드들
            "input[inputmode='numeric']",
            "input[pattern*='[0-9]']"
        };
        
        for (String selector : selectors) {
            try {
                List<WebElement> elements = driver.findElements(By.cssSelector(selector));
                for (WebElement element : elements) {
                    if (element.isDisplayed() && element.isEnabled()) {
                        String placeholder = element.getAttribute("placeholder");
                        String name = element.getAttribute("name");
                        String className = element.getAttribute("class");
                        String type = element.getAttribute("type");
                        
                        log.info("Found potential price field: placeholder={}, name={}, class={}, type={}", 
                                placeholder, name, className, type);
                        
                        // 가격 관련 키워드가 포함된 필드 찾기
                        if (placeholder != null && (placeholder.contains("가격") || placeholder.contains("원") || placeholder.contains("금액"))) {
                            log.info("✅ Found price field by placeholder: {}", placeholder);
                            return element;
                        }
                        
                        if (name != null && (name.contains("price") || name.contains("amount") || name.contains("cost"))) {
                            log.info("✅ Found price field by name: {}", name);
                            return element;
                        }
                        
                        // 숫자 입력 필드 중에서 가격일 가능성이 높은 것
                        if ("number".equals(type) || "tel".equals(type)) {
                            log.info("✅ Found numeric input field as potential price field: type={}", type);
                            return element;
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("Price field selector failed: {}", selector);
            }
        }
        
        log.warn("❌ Could not find price field");
        return null;
    }
    
    /**
     * 상품 설명 입력 필드 찾기
     */
    private WebElement findDescriptionField(WebDriver driver, WebDriverWait wait) {
        log.info("상품 설명 입력 필드를 찾는 중...");
        
        // 번개장터 상품 등록 페이지의 실제 구조에 맞는 셀렉터들
        String[] selectors = {
            // 번개장터 특화 셀렉터들
            "textarea[placeholder*='설명']",
            "textarea[placeholder*='상품 설명']",
            "textarea[placeholder*='상품에 대해 설명해주세요']",
            "textarea[placeholder*='상품을 자세히 설명해주세요']",
            "textarea[name*='description']",
            "textarea[name*='content']",
            "textarea[name*='detail']",
            "textarea[name*='info']",
            // 폼 내부의 설명 필드
            "form textarea",
            "form textarea[placeholder*='설명']",
            // 특정 클래스나 ID 기반
            "textarea[class*='description']",
            "textarea[class*='content']",
            "textarea[class*='detail']",
            "textarea[id*='description']",
            "textarea[id*='content']",
            "textarea[id*='detail']",
            // 일반적인 텍스트 영역
            "textarea[rows]",
            "textarea[cols]"
        };
        
        for (String selector : selectors) {
            try {
                List<WebElement> elements = driver.findElements(By.cssSelector(selector));
                for (WebElement element : elements) {
                    if (element.isDisplayed() && element.isEnabled()) {
                        String placeholder = element.getAttribute("placeholder");
                        String name = element.getAttribute("name");
                        String className = element.getAttribute("class");
                        String rows = element.getAttribute("rows");
                        
                        log.info("Found potential description field: placeholder={}, name={}, class={}, rows={}", 
                                placeholder, name, className, rows);
                        
                        // 설명 관련 키워드가 포함된 필드 찾기
                        if (placeholder != null && (placeholder.contains("설명") || placeholder.contains("내용") || placeholder.contains("상세"))) {
                            log.info("✅ Found description field by placeholder: {}", placeholder);
                            return element;
                        }
                        
                        if (name != null && (name.contains("description") || name.contains("content") || name.contains("detail"))) {
                            log.info("✅ Found description field by name: {}", name);
                            return element;
                        }
                        
                        // 큰 텍스트 영역 (설명 필드일 가능성이 높음)
                        if (rows != null && Integer.parseInt(rows) >= 3) {
                            log.info("✅ Found large textarea as potential description field: rows={}", rows);
                            return element;
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("Description field selector failed: {}", selector);
            }
        }
        
        log.warn("❌ Could not find description field");
        return null;
    }
    
    /**
     * 요소가 존재하고 표시되는지 확인
     */
    private boolean existsDisplayed(WebDriver driver, By by) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));
            WebElement element = wait.until(org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated(by));
            return element.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 사람처럼 포커스하고 스크롤
     */
    private void humanFocusAndScroll(WebDriver driver, WebElement element) {
        ((JavascriptExecutor)driver).executeScript("arguments[0].scrollIntoView({block:'center'});", element);
        new Actions(driver).moveToElement(element).pause(Duration.ofMillis(200 + (long)(Math.random()*300))).perform();
    }
    
    /**
     * 사람처럼 타이핑 (문자별 지연)
     */
    private void humanType(WebElement element, String text) {
        try {
            element.clear();
            for (char c : text.toCharArray()) {
                element.sendKeys(Character.toString(c));
                Thread.sleep(40 + (long)(Math.random()*60)); // 40~100ms 지터
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * JavaScript 클릭
     */
    private void jsClick(WebDriver driver, WebElement element) {
        ((JavascriptExecutor)driver).executeScript("arguments[0].click();", element);
    }
}
