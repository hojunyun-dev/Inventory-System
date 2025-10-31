package com.inventory.registration.service.bunjang;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

/**
 * 번개장터 로그인 처리 클래스
 * - 로그인 상태 확인
 * - 로그인 버튼 찾기 및 클릭
 * - 네이버 로그인 처리
 * - 로그인 완료 감지
 */
@Component
@Slf4j
public class BunjangLoginHandler {
    
    
    @Autowired
    private BunjangUtils utils;
    
    /**
     * 차단/레이트리밋/보안 확인 감지 (거의 비활성화)
     */
    private boolean isBlockedOrRateLimited(WebDriver d) {
        try {
            String currentUrl = d.getCurrentUrl().toLowerCase();
            
            // 🚀 차단 감지 거의 완전 비활성화 - 명확한 외부 리다이렉트만 감지
            if (currentUrl.contains("facebook.com") || currentUrl.contains("kakao.com")) {
                log.warn("🚫 Detected redirect to external site: {}", currentUrl);
                return true;
            }
            
            // 🚀 번개장터에서도 차단 감지 최소화 - 명확한 차단 페이지만 감지
            if (currentUrl.contains("bunjang.co.kr")) {
                String html = d.getPageSource().toLowerCase();
                
                // 🚀 극도로 완화된 차단 감지 - 명확한 차단 페이지만 감지
                boolean blocked = html.contains("접근이 차단되었습니다")
                    || html.contains("access denied")
                    || html.contains("too many requests")
                    || html.contains("rate limit exceeded");
                    
                if (blocked) {
                    log.warn("🚫 Detected clear blocking page");
                }
                
                return blocked;
            }
            
            return false;
        } catch (Exception e) { 
            log.debug("Error checking for blocking: {}", e.getMessage());
            return false; 
        }
    }
    
    /**
     * IP 차단 및 봇감지 식별 및 롤백 처리 (거의 비활성화)
     */
    public boolean handleBlockingAndRollback(WebDriver driver, BunjangWebDriverManager webDriverManager) {
        log.info("🔍 Checking for IP blocking and bot detection...");
        
        try {
            String currentUrl = driver.getCurrentUrl();
            
            // 🚀 차단 감지 거의 완전 비활성화 - 명확한 외부 리다이렉트만 감지
            boolean isRedirected = currentUrl.contains("facebook.com") || currentUrl.contains("kakao.com");
            
            // 네이버 로그인 페이지에서는 차단 감지 완전 비활성화
            if (currentUrl.contains("nid.naver.com") || currentUrl.contains("naver.com")) {
                log.debug("Naver login page detected - skipping blocking detection");
                return false;
            }
            
            // 🚀 극도로 완화된 차단 조건 - 외부 리다이렉트만 감지
            if (isRedirected) {
                log.warn("🚨 External redirect detected: {}", currentUrl);
                
                // 2. 롤백 처리
                return performRollback(driver, webDriverManager);
            }
            
            return false;
            
        } catch (Exception e) {
            log.error("Error in blocking detection: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 롤백 처리 (브라우저 재시작 및 재시도) - 세션 유지 개선
     */
    private boolean performRollback(WebDriver driver, BunjangWebDriverManager webDriverManager) {
        log.info("🔄 Performing rollback...");
        
        try {
            // 1. 현재 브라우저 종료
            log.info("🔄 Closing current browser...");
            try {
                driver.quit();
            } catch (Exception e) {
                log.warn("Error closing driver: {}", e.getMessage());
            }
            
            // 2. 잠시 대기 (속도 최적화)
            Thread.sleep(500);  // 1초 → 0.5초로 단축
            
            // 3. 새로운 브라우저 생성
            log.info("🔄 Creating new browser instance...");
            WebDriver newDriver = webDriverManager.createWebDriver();
            
            // 4. 번개장터 홈페이지로 이동 (데스크톱 페이지 강제 사용)
            log.info("🔄 Navigating to bunjang desktop homepage...");
            newDriver.get("https://www.bunjang.co.kr?desktop=1&force_desktop=true");
            Thread.sleep(500);  // 1초 → 0.5초로 단축
            
            // 5. 자연스러운 동작 시뮬레이션 (속도 최적화)
            utils.simulateMouseMovement(newDriver);
            // utils.simulateNaturalScrolling(newDriver);  // 스크롤링 제거로 속도 향상
            
            log.info("✅ Rollback completed successfully");
            return true;
            
        } catch (Exception e) {
            log.error("❌ Rollback failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 로그인 플로우(버튼 클릭 → 네이버 버튼 → 새창 → 네이버로 이동 보장)
     */
    public void runLoginFlow(WebDriver d, BunjangWebDriverManager wdm) {
        log.info("🔐 Starting login flow...");
        
        // 브라우저 세션 유효성 확인 (완화된 검사)
        try {
            if (!wdm.isSessionValid(d)) {
                log.warn("⚠️ 브라우저 세션이 불안정하지만 계속 진행합니다.");
                // 세션이 불안정해도 계속 진행
            } else {
                log.info("✅ 브라우저 세션 유효성 확인 완료");
            }
        } catch (Exception e) {
            log.warn("⚠️ 브라우저 세션 검사 중 오류 발생하지만 계속 진행: {}", e.getMessage());
        }
        
        // 🚀 초고속 홈페이지 이동 (3초 → 1초로 단축)
        if (d.getCurrentUrl() == null || !d.getCurrentUrl().contains("bunjang.co.kr") || d.getCurrentUrl().contains("m.bunjang.co.kr")) {
            log.info("🚀 Fast navigation to bunjang homepage...");
            d.navigate().to("https://www.bunjang.co.kr?desktop=1&force_desktop=true");
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}  // 3초 → 1초로 단축
            
            // 모바일 페이지로 리다이렉트되는 경우 다시 데스크톱 페이지로 강제 이동
            if (d.getCurrentUrl().contains("m.bunjang.co.kr")) {
                log.info("Mobile redirect detected, forcing desktop page...");
                d.navigate().to("https://www.bunjang.co.kr?desktop=1&force_desktop=true");
                try { Thread.sleep(500); } catch (InterruptedException ignored) {}  // 2초 → 0.5초로 단축
            }
        }

        log.info("Looking for login/signup button...");
        
        // 🚀 초고속 로그인 버튼 찾기 (정확한 셀렉터 우선)
        WebElement login = null;
        
        // CSS 셀렉터와 XPath 셀렉터를 모두 시도
        String[] loginSelectors = {
            "button.sc-dqBHgY.dDTfxq",  // CSS 정확한 클래스 셀렉터 (우선순위 1)
            "button.sc-dqBHgY",  // CSS 부분 클래스 셀렉터 (우선순위 2)
            "button.dDTfxq",  // CSS 부분 클래스 셀렉터 (우선순위 3)
            "//button[@class='sc-dqBHgY dDTfxq']",  // XPath 정확한 클래스 셀렉터 (우선순위 4)
            "//button[contains(@class,'sc-dqBHgY')]",  // XPath 부분 클래스 셀렉터 (우선순위 5)
            "//button[contains(@class,'dDTfxq')]",  // XPath 부분 클래스 셀렉터 (우선순위 6)
            "//button[contains(text(),'로그인/회원가입')]",  // XPath 텍스트 기반 (우선순위 7)
            "//button[contains(@class,'sc-')]",  // XPath sc- 클래스 패턴 (우선순위 8)
            "//a[contains(text(),'로그인/회원가입')]",  // XPath 링크 형태 (우선순위 9)
            "//*[contains(@class,'login')]",  // XPath 클래스 기반 (우선순위 10)
            "//*[contains(@id,'login')]"  // XPath ID 기반 (우선순위 11)
        };
        
        for (String selector : loginSelectors) {
            try {
                login = new WebDriverWait(d, Duration.ofMillis(50))  // 각 셀렉터당 0.05초만 대기 (초고속)
                    .until(ExpectedConditions.elementToBeClickable(
                        selector.startsWith("//") ? By.xpath(selector) : By.cssSelector(selector)));
                log.info("✅ Found login button with selector: {}", selector);
                break;
            } catch (Exception e) {
                log.debug("Login button not found with selector: {}", selector);
            }
        }
        
        if (login == null) {
            throw new RuntimeException("로그인/회원가입 버튼을 찾을 수 없습니다");
        }
        log.info("✅ Found login button: {}", login.getText());
        
        // 🚀 초고속 자연스러운 동작 시뮬레이션 (시간 단축)
        utils.simulateMouseMovement(d);
        // utils.simulateNaturalScrolling(d);  // 스크롤링 제거로 속도 향상
        
        // 🚀 초고속 자연스러운 클릭
        utils.humanClick(d, login);
        log.info("✅ Clicked login button naturally");

        // 🚀 초고속 팝업 대기 (50ms로 단축)
        try { Thread.sleep(50); } catch (InterruptedException ignored) {}  // 100ms → 50ms로 단축

        log.info("Looking for Naver login button...");
        
        // 🚀 초고속 네이버 버튼 찾기
        WebElement naver = null;
        String[] naverSelectors = {
            "//button[contains(text(),'네이버로 이용하기')]",
            "//a[contains(text(),'네이버로 이용하기')]",
            "//*[contains(text(),'네이버로 이용하기')]",
            "//button[contains(@class,'naver')]",
            "//a[contains(@class,'naver')]",
            "//*[@data-provider='naver']",
            "//button[contains(@onclick,'naver')]"
        };
        
        for (String selector : naverSelectors) {
            try {
                naver = new WebDriverWait(d, Duration.ofMillis(300))  // 1초 → 0.3초로 단축
                    .until(ExpectedConditions.elementToBeClickable(By.xpath(selector)));
                log.info("✅ Found Naver button with selector: {}", selector);
                break;
            } catch (Exception e) {
                log.debug("Naver button not found with selector: {}", selector);
            }
        }
        
        if (naver == null) {
            throw new RuntimeException("네이버 로그인 버튼을 찾을 수 없습니다");
        }
        
        log.info("✅ Found Naver button: {}", naver.getText());
        
        // 자연스러운 동작 시뮬레이션
        utils.simulateMouseMovement(d);
        
        // 자연스러운 클릭
        utils.humanClick(d, naver);
        log.info("✅ Clicked Naver button naturally");

        // 새창 전환 (headless 포함)
        log.info("Checking for new window...");
        boolean switched = wdm.switchToNewWindowIfOpened(d, 10);
        if (switched) {
            log.info("✅ Switched to new window");
        } else {
            log.info("No new window detected, staying on current window");
        }

        // 네이버 도메인 확인
        try { 
            Thread.sleep(1000); 
        } catch (InterruptedException ignored) {}
        String url = d.getCurrentUrl();
        log.info("After Naver click, current URL: {}", url);

        // 네이버 로그인 페이지인지 확인
        if (url.contains("nid.naver.com") || url.contains("naver.com")) {
            log.info("✅ Successfully navigated to Naver login page");
        } else if (url.contains("bunjang.co.kr")) {
            log.info("⚠️ Still on Bunjang page, may need manual intervention");
        } else {
            log.warn("⚠️ Unexpected URL after Naver click: {}", url);
        }

        if (isBlockedOrRateLimited(d)) {
            throw new RuntimeException("로그인 차단/보안 확인 페이지 감지");
        }
        
        log.info("🔐 Login flow completed");
    }
    
    /**
     * 판매하기 버튼 셀렉터 디버깅 메서드
     */
    public void debugSellButtonSelectors(WebDriver driver) {
        log.info("🔍 Debugging sell button selectors...");
        
        try {
            String currentUrl = driver.getCurrentUrl();
            log.info("Current URL: {}", currentUrl);
            
            // 페이지 HTML 소스에서 판매하기 관련 텍스트 찾기
            String pageSource = driver.getPageSource();
            
            // 판매하기 관련 텍스트가 있는지 확인
            if (pageSource.contains("판매하기")) {
                log.info("✅ Found '판매하기' text in page source");
                
                // HTML에서 판매하기 버튼 부분 추출
                String[] lines = pageSource.split("\n");
                for (int i = 0; i < lines.length; i++) {
                    if (lines[i].contains("판매하기")) {
                        log.info("Line {}: {}", i, lines[i].trim());
                        // 주변 라인도 확인
                        for (int j = Math.max(0, i-2); j <= Math.min(lines.length-1, i+2); j++) {
                            if (j != i) {
                                log.info("  Line {}: {}", j, lines[j].trim());
                            }
                        }
                        break;
                    }
                }
            } else {
                log.warn("❌ '판매하기' text not found in page source");
            }
            
            // 실제 HTML 구조에 맞춘 최적화된 셀렉터들 (모바일 페이지 지원)
            String[] prioritySelectors = {
                "//a[contains(@class,'sc-eXEjpC') and contains(text(),'판매하기')]",  // 가장 정확한 셀렉터
                "//a[@class='sc-eXEjpC BltZS' and contains(text(),'판매하기')]",  // 모바일 페이지용 정확한 셀렉터
                "//a[contains(@href,'products/new')]",  // href 기반 (매우 정확)
                "//a[contains(text(),'판매하기') and .//img[@alt='판매하기버튼 이미지']]",  // 이미지 alt 텍스트 기반
                "//a[contains(text(),'판매하기')]",  // 텍스트 기반
                "//*[contains(text(),'판매하기')]"  // 백업용
            };
            
            for (String selector : prioritySelectors) {
                try {
                    List<WebElement> elements = driver.findElements(By.xpath(selector));
                    if (!elements.isEmpty()) {
                        log.info("✅ Found {} elements with selector: {}", elements.size(), selector);
                        for (int i = 0; i < Math.min(elements.size(), 2); i++) {
                            WebElement element = elements.get(i);
                            log.info("  Element {}: tag={}, text='{}', href='{}', class='{}'", 
                                i, element.getTagName(), element.getText(), 
                                element.getAttribute("href"), element.getAttribute("class"));
                        }
                        // 첫 번째로 찾은 셀렉터가 있으면 여기서 중단
                        break;
                    } else {
                        log.debug("❌ No elements found with selector: {}", selector);
                    }
                } catch (Exception e) {
                    log.debug("Error with selector {}: {}", selector, e.getMessage());
                }
            }
            
        } catch (Exception e) {
            log.error("Error debugging sell button selectors: {}", e.getMessage());
        }
    }

    /**
     * 로그인 상태 확인 (모바일 페이지 최적화)
     */
    public boolean isLoggedIn(WebDriver driver) {
        log.info("🔍 Mobile-optimized login status check...");

        try {
            // 페이지 로딩 대기
            try {
                Thread.sleep(1000);
            } catch (Exception ignore) {}

            String currentUrl = driver.getCurrentUrl();
            log.info("Current URL: {}", currentUrl);

            // 1. URL이 번개장터 도메인이 아닌 경우 로그인 안됨
            if (!currentUrl.contains("bunjang.co.kr")) {
                log.info("❌ Not on bunjang domain - not logged in");
                return false;
            }

            // 2. 모바일 페이지 감지
            boolean isMobilePage = currentUrl.contains("m.bunjang.co.kr");
            log.info("📱 Mobile page detected: {}", isMobilePage);

            // 3. 모바일 페이지 최적화된 로그인 상태 감지
            if (isMobilePage) {
                return checkMobileLoginStatus(driver);
            } else {
                return checkDesktopLoginStatus(driver);
            }
            
        } catch (Exception e) {
            log.error("Error checking login status: {}", e.getMessage());
            return false;
        }
    }

           /**
            * 모바일 페이지 로그인 상태 확인 (실제 번개장터 UI 기반)
            */
           private boolean checkMobileLoginStatus(WebDriver driver) {
               try {
                   log.info("🔍 Checking mobile login status...");
                   
                   // 로그인 성공 조건들 (로그인 완료 시에만 나타나는 요소들)
                   boolean hasLogoutButton = checkElementExists(driver, "//button[contains(text(),'로그아웃')]", "로그아웃 버튼");
                   boolean hasNotification = checkElementExists(driver, "//a[contains(text(),'알림')]", "알림 버튼");
                   boolean hasMyShopDropdown = checkElementExists(driver, "//div[contains(@class,'sc-dnqmqq')]", "내 상점 드롭다운");
                   boolean hasAccountSettings = checkElementExists(driver, "//a[contains(text(),'계정설정')]", "계정설정 링크");
                   boolean hasMyProducts = checkElementExists(driver, "//a[contains(text(),'내 상품')]", "내 상품 링크");
                   boolean hasFavorites = checkElementExists(driver, "//a[contains(text(),'찜한상품')]", "찜한상품 링크");
                   
                   // 로그인 성공 조건들 (6개 중 2개 이상 만족하면 성공)
                   int successCount = 0;
                   if (hasLogoutButton) successCount++;
                   if (hasNotification) successCount++;
                   if (hasMyShopDropdown) successCount++;
                   if (hasAccountSettings) successCount++;
                   if (hasMyProducts) successCount++;
                   if (hasFavorites) successCount++;
                   
                   log.info("🔎 Mobile login check: logout={}, notification={}, myShop={}, account={}, products={}, favorites={}, successCount={}/6", 
                           hasLogoutButton, hasNotification, hasMyShopDropdown, hasAccountSettings, hasMyProducts, hasFavorites, successCount);

                   // 로그인 성공 조건: 6개 조건 중 2개 이상 만족하면 로그인 성공
                   if (successCount >= 2) {
                       log.info("✅ Mobile login success: {}/6 conditions met", successCount);
                       return true;
                   }

                   log.info("❌ Mobile login failed: Only {}/6 conditions met", successCount);
                   return false;

               } catch (Exception e) {
                   log.error("❌ Error checking mobile login status: {}", e.getMessage());
                   return false;
               }
           }

           /**
            * 데스크톱 페이지 로그인 상태 확인 (실제 번개장터 UI 기반)
            */
           private boolean checkDesktopLoginStatus(WebDriver driver) {
               try {
                   log.info("🔍 Checking desktop login status...");
                   
                   // 로그인 성공 조건들 (로그인 완료 시에만 나타나는 요소들)
                   boolean hasLogoutButton = checkElementExists(driver, "//button[contains(text(),'로그아웃')]", "로그아웃 버튼");
                   boolean hasNotification = checkElementExists(driver, "//a[contains(text(),'알림')]", "알림 버튼");
                   boolean hasMyShopDropdown = checkElementExists(driver, "//div[contains(@class,'sc-dnqmqq')]", "내 상점 드롭다운");
                   boolean hasAccountSettings = checkElementExists(driver, "//a[contains(text(),'계정설정')]", "계정설정 링크");
                   boolean hasMyProducts = checkElementExists(driver, "//a[contains(text(),'내 상품')]", "내 상품 링크");
                   boolean hasFavorites = checkElementExists(driver, "//a[contains(text(),'찜한상품')]", "찜한상품 링크");
                   
                   // 로그인 성공 조건들 (6개 중 2개 이상 만족하면 성공)
                   int successCount = 0;
                   if (hasLogoutButton) successCount++;
                   if (hasNotification) successCount++;
                   if (hasMyShopDropdown) successCount++;
                   if (hasAccountSettings) successCount++;
                   if (hasMyProducts) successCount++;
                   if (hasFavorites) successCount++;
                   
                   log.info("🔎 Desktop login check: logout={}, notification={}, myShop={}, account={}, products={}, favorites={}, successCount={}/6", 
                           hasLogoutButton, hasNotification, hasMyShopDropdown, hasAccountSettings, hasMyProducts, hasFavorites, successCount);

                   // 로그인 성공 조건: 6개 조건 중 2개 이상 만족하면 로그인 성공
                   if (successCount >= 2) {
                       log.info("✅ Desktop login success: {}/6 conditions met", successCount);
                       return true;
                   }

                   log.info("❌ Desktop login failed: Only {}/6 conditions met", successCount);
                   return false;

               } catch (Exception e) {
                   log.error("❌ Error checking desktop login status: {}", e.getMessage());
                   return false;
               }
           }
    
    
    
    
    
           /**
            * 요소 존재 확인 (CSS Selector와 XPath 지원)
            */
           private boolean checkElementExists(WebDriver driver, String selector, String elementName) {
               try {
                   WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));
                   WebElement element;
                   
                   // XPath인지 CSS Selector인지 판단
                   if (selector.startsWith("//") || selector.startsWith("(//")) {
                       element = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(selector)));
                   } else {
                       element = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(selector)));
                   }
                   
                   return element.isDisplayed();
               } catch (Exception e) {
                   log.debug("Element not found: {} - {}", elementName, selector);
                   return false;
               }
           }

    
    /**
     * 로그인 버튼 찾기
     */
    public WebElement findLoginButton(WebDriver driver) {
        log.info("로그인 버튼을 찾는 중...");
        
        String[] selectors = {
            "//a[contains(text(),'로그인')]",
            "//button[contains(text(),'로그인')]",
            "//a[contains(text(),'회원가입')]",
            "//button[contains(text(),'회원가입')]",
            "//a[contains(@class,'login')]",
            "//button[contains(@class,'login')]",
            "//a[contains(@href,'login')]",
            "//button[contains(@href,'login')]"
        };
        
        for (String selector : selectors) {
            try {
                List<WebElement> elements = driver.findElements(By.xpath(selector));
                for (WebElement element : elements) {
                    if (element.isDisplayed() && element.isEnabled()) {
                        String text = element.getText();
                        log.info("Found login button: {}", text);
                        return element;
                    }
                }
            } catch (Exception e) {
                log.debug("Login button selector failed: {}", selector);
            }
        }
        
        log.warn("❌ Could not find login button");
        return null;
    }
    
    /**
     * 네이버 로그인 버튼 찾기
     */
    public WebElement findNaverButton(WebDriver driver) {
        log.info("Looking for Naver login button...");
        
        // 더 정확한 셀렉터들 (번개장터 실제 구조에 맞게)
        String[] selectors = {
            "//button[contains(text(), '네이버로 이용하기')]",
            "//a[contains(text(), '네이버로 이용하기')]",
            "//button[contains(text(), '네이버')]",
            "//a[contains(text(), '네이버')]",
            "//button[contains(@class, 'naver')]",
            "//a[contains(@class, 'naver')]",
            "//button[contains(@data-provider, 'naver')]",
            "//a[contains(@data-provider, 'naver')]",
            "//button[contains(@aria-label, '네이버')]",
            "//a[contains(@aria-label, '네이버')]",
            "//*[@id='naver-login']",
            "//*[@class*='naver' and @class*='login']",
            "//button[contains(@onclick, 'naver')]",
            "//a[contains(@href, 'naver')]"
        };
        
        for (String selector : selectors) {
            try {
                List<WebElement> elements = driver.findElements(By.xpath(selector));
                for (WebElement element : elements) {
                    if (element.isDisplayed() && element.isEnabled()) {
                        String text = element.getText();
                        String tagName = element.getTagName();
                        log.info("Found potential Naver element: tag={}, text={}, selector={}", tagName, text, selector);
                        
                        if (text.contains("네이버") || text.contains("NAVER")) {
                            log.info("✅ Found Naver button: {}", text);
                            return element;
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("Naver button selector failed: {}", selector);
            }
        }
        
        // 추가: 페이지 소스에서 네이버 관련 텍스트 찾기
        try {
            String pageSource = driver.getPageSource();
            if (pageSource.contains("네이버") || pageSource.contains("naver")) {
                log.info("네이버 관련 텍스트가 페이지에 있지만 버튼을 찾지 못했습니다. 페이지 구조 확인 필요.");
                log.debug("페이지 소스에서 네이버 관련 부분: {}", 
                    pageSource.substring(Math.max(0, pageSource.indexOf("네이버") - 100), 
                                       Math.min(pageSource.length(), pageSource.indexOf("네이버") + 200)));
            }
        } catch (Exception e) {
            log.debug("페이지 소스 확인 실패: {}", e.getMessage());
        }
        
        log.warn("❌ Could not find Naver login button");
        return null;
    }
    
    /**
     * 로그인 페이지로 이동
     */
    public void navigateToLoginPage(WebDriver driver) {
        log.info("로그인 페이지로 이동 중...");
        driver.get("https://m.bunjang.co.kr/login"); // 모바일 로그인 페이지로 직접 이동
        try {
            Thread.sleep(2000); // 페이지 로딩 대기
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("로그인 페이지로 이동 완료. 현재 URL: {}", driver.getCurrentUrl());
    }

    /**
     * 로그인 완료 감지 (개선된 버전)
     */
    public boolean waitForLoginComplete(WebDriver driver, long maxSec) {
        log.info("⏳ 로그인 완료 감지 시작 ({}s)", maxSec);
        long end = System.currentTimeMillis() + maxSec * 1000L;

        while (System.currentTimeMillis() < end) {
            try {
                // 1. 현재 URL 확인
                String currentUrl = driver.getCurrentUrl();
                log.debug("Current URL: {}", currentUrl);

                // 2. 로그인 상태 확인
                if (isLoggedIn(driver)) {
                    log.info("✅ 로그인 완료 감지됨!");
                    return true;
                }

                // 3. 네이버 로그인 페이지에서는 대기 (사용자 수동 로그인)
                if (currentUrl.contains("nid.naver.com") || currentUrl.contains("naver.com")) {
                    log.debug("네이버 로그인 페이지 감지. 사용자 로그인 대기 중...");
                    Thread.sleep(2000); // 네이버 로그인 완료 대기
                } else if (currentUrl.contains("facebook.com") || currentUrl.contains("kakao.com")) {
                    log.warn("외부 로그인 페이지로 리다이렉트됨: {}", currentUrl);
                    // 외부 로그인 페이지로 리다이렉트된 경우, 다시 번개장터 로그인 페이지로 이동 시도
                    navigateToLoginPage(driver);
                } else if (!currentUrl.contains("bunjang.co.kr/login") && !currentUrl.contains("bunjang.co.kr")) {
                    log.warn("번개장터 도메인을 벗어남. 로그인 페이지로 다시 이동: {}", currentUrl);
                    navigateToLoginPage(driver);
                }

                Thread.sleep(1000); // 1초 대기 후 재시도
            } catch (Exception e) {
                log.warn("로그인 감지 중 오류 발생: {}", e.getMessage());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {}
            }
        }

        log.warn("❌ 로그인 완료 감지 타임아웃 ({}s)", maxSec);
        return false;
    }
    
}
