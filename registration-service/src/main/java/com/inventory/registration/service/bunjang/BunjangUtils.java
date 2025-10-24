package com.inventory.registration.service.bunjang;

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

/**
 * 번개장터 공통 유틸리티 클래스
 * - 사람처럼 행동하는 유틸리티 함수들
 * - 대기 및 지연 함수들
 * - 공통 셀렉터 및 검증 함수들
 */
@Component
@Slf4j
public class BunjangUtils {
    
    /**
     * WebDriverWait 인스턴스 생성
     */
    public WebDriverWait createWebDriverWait(WebDriver driver, long seconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(seconds));
    }
    
    /**
     * 사람처럼 일시정지 (가변 시간)
     */
    public void humanPause(int minMs, int maxMs) {
        try {
            Thread.sleep(minMs + (long)(Math.random()*(maxMs-minMs)));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 사람처럼 타이핑 (문자별 지연)
     */
    public void humanType(WebElement element, String text) {
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
     * 사람처럼 포커스하고 스크롤
     */
    public void humanFocusAndScroll(WebDriver driver, WebElement element) {
        ((JavascriptExecutor)driver).executeScript("arguments[0].scrollIntoView({block:'center'});", element);
        new Actions(driver).moveToElement(element).pause(Duration.ofMillis(200 + (long)(Math.random()*300))).perform();
    }
    
    /**
     * 사람처럼 자연스러운 클릭 (마우스 움직임 포함)
     */
    public void humanClick(WebDriver driver, WebElement element) {
        try {
            Actions actions = new Actions(driver);
            
            // 1. 요소로 스크롤
            ((JavascriptExecutor)driver).executeScript("arguments[0].scrollIntoView({block:'center'});", element);
            humanPause(500, 800);
            
            // 2. 요소 위로 마우스 이동 (자연스러운 경로)
            actions.moveToElement(element).pause(Duration.ofMillis(300 + (long)(Math.random()*400))).perform();
            humanPause(200, 400);
            
            // 3. 클릭
            actions.click(element).perform();
            humanPause(300, 600);
            
        } catch (Exception e) {
            log.debug("자연스러운 클릭 실패, JavaScript 클릭으로 대체: {}", e.getMessage());
            jsClick(driver, element);
        }
    }
    
    /**
     * 사람처럼 자연스러운 마우스 움직임
     */
    public void humanMouseMove(WebDriver driver, WebElement element) {
        try {
            Actions actions = new Actions(driver);
            
            // 요소로 자연스럽게 이동
            actions.moveToElement(element).pause(Duration.ofMillis(500 + (long)(Math.random()*800))).perform();
            humanPause(300, 600);
            
        } catch (Exception e) {
            log.debug("자연스러운 마우스 움직임 실패: {}", e.getMessage());
        }
    }
    
    /**
     * JavaScript 클릭
     */
    public void jsClick(WebDriver driver, WebElement element) {
        ((JavascriptExecutor)driver).executeScript("arguments[0].click();", element);
    }
    
    /**
     * DOM 준비 상태 대기
     */
    public void waitDomReady(WebDriver driver) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("return document.readyState");
        } catch (Exception e) {
            log.debug("페이지 제목 확인 실패: {}", e.getMessage());
            humanPause(500, 1000); // 대기시간 단축
        }
    }
    
    /**
     * 요소가 존재하고 표시되는지 확인
     */
    public boolean existsDisplayed(WebDriver driver, By by) {
        try {
            WebDriverWait wait = createWebDriverWait(driver, 1); // 대기시간 단축
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(by));
            return element.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 요소가 보이지 않을 때까지 대기 (있으면)
     */
    public boolean invisibileIfPresent(WebDriver driver, By by) {
        try {
            WebDriverWait wait = createWebDriverWait(driver, 2);
            return wait.until(ExpectedConditions.invisibilityOfElementLocated(by));
        } catch (Exception e) {
            return true; // 없거나 이미 안보임 → true 간주
        }
    }
    
    /**
     * 요소가 클릭 가능할 때까지 대기
     */
    public WebElement waitForClickable(WebDriver driver, By by, long seconds) {
        try {
            WebDriverWait wait = createWebDriverWait(driver, seconds);
            return wait.until(ExpectedConditions.elementToBeClickable(by));
        } catch (Exception e) {
            log.debug("Element not clickable: {}", by);
            return null;
        }
    }
    
    /**
     * 요소가 존재할 때까지 대기
     */
    public WebElement waitForPresence(WebDriver driver, By by, long seconds) {
        try {
            WebDriverWait wait = createWebDriverWait(driver, seconds);
            return wait.until(ExpectedConditions.presenceOfElementLocated(by));
        } catch (Exception e) {
            log.debug("Element not present: {}", by);
            return null;
        }
    }
    
    /**
     * 요소가 보일 때까지 대기
     */
    public WebElement waitForVisible(WebDriver driver, By by, long seconds) {
        try {
            WebDriverWait wait = createWebDriverWait(driver, seconds);
            return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
        } catch (Exception e) {
            log.debug("Element not visible: {}", by);
            return null;
        }
    }
    
    /**
     * 페이지 제목 확인
     */
    public String getPageTitle(WebDriver driver) {
        try {
            return driver.getTitle();
        } catch (Exception e) {
            log.debug("페이지 제목 확인 실패: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 현재 URL 확인
     */
    public String getCurrentUrl(WebDriver driver) {
        try {
            return driver.getCurrentUrl();
        } catch (Exception e) {
            log.debug("현재 URL 확인 실패: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 페이지 소스에서 특정 텍스트 검색
     */
    public boolean containsText(WebDriver driver, String text) {
        try {
            String pageSource = driver.getPageSource();
            return pageSource.contains(text);
        } catch (Exception e) {
            log.debug("페이지 소스 검색 실패: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 새 창/탭으로 전환
     */
    public boolean switchToNewWindow(WebDriver driver) {
        try {
            String originalWindow = driver.getWindowHandle();
            
            // 새 창이 열릴 때까지 대기
            humanPause(1000, 2000);
            
            for (String windowHandle : driver.getWindowHandles()) {
                if (!originalWindow.equals(windowHandle)) {
                    driver.switchTo().window(windowHandle);
                    return true;
                }
            }
            
            return false;
        } catch (Exception e) {
            log.debug("새 창 전환 실패: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 원래 창으로 돌아가기
     */
    public void switchToOriginalWindow(WebDriver driver, String originalWindow) {
        try {
            driver.switchTo().window(originalWindow);
        } catch (Exception e) {
            log.debug("원래 창 전환 실패: {}", e.getMessage());
        }
    }
    
    /**
     * 스크린샷 촬영 (디버깅용)
     */
    public void takeScreenshot(WebDriver driver, String filename) {
        try {
            // 실제 스크린샷 기능은 필요시 구현
            log.info("스크린샷 촬영: {}", filename);
        } catch (Exception e) {
            log.debug("스크린샷 촬영 실패: {}", e.getMessage());
        }
    }
    
    /**
     * 로그인 상태 확인을 위한 쿠키 검사
     */
    public boolean hasAuthenticationCookie(WebDriver driver, String cookieName) {
        try {
            return driver.manage().getCookieNamed(cookieName) != null;
        } catch (Exception e) {
            log.debug("쿠키 확인 실패: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 페이지 로드 완료 대기
     */
    public void waitForPageLoad(WebDriver driver, long timeoutSeconds) {
        try {
            WebDriverWait wait = createWebDriverWait(driver, timeoutSeconds);
            wait.until(driver1 -> {
                JavascriptExecutor js = (JavascriptExecutor) driver1;
                return js.executeScript("return document.readyState").equals("complete");
            });
        } catch (Exception e) {
            log.debug("페이지 로드 대기 실패: {}", e.getMessage());
        }
    }
    
    /**
     * User-Agent 로테이션을 위한 랜덤 User-Agent 생성
     */
    public String getRandomUserAgent() {
        String[] userAgents = {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:121.0) Gecko/20100101 Firefox/121.0",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:121.0) Gecko/20100101 Firefox/121.0",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Edge/120.0.0.0 Safari/537.36"
        };
        
        return userAgents[(int) (Math.random() * userAgents.length)];
    }
    
    /**
     * 프록시 서버 리스트 (실제 사용시 유료 프록시 서비스 연동 필요)
     */
    public String[] getProxyList() {
        // 실제 구현시에는 프록시 서비스 API에서 가져오거나 설정 파일에서 로드
        return new String[]{
            "proxy1.example.com:8080",
            "proxy2.example.com:8080",
            "proxy3.example.com:8080"
        };
    }
    
    /**
     * 랜덤 프록시 선택
     */
    public String getRandomProxy() {
        String[] proxies = getProxyList();
        return proxies[(int) (Math.random() * proxies.length)];
    }
    
    /**
     * 자연스러운 스크롤 시뮬레이션
     */
    public void simulateNaturalScrolling(WebDriver driver) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            
            // 페이지 높이의 20-80% 사이에서 랜덤 스크롤
            Long pageHeight = (Long) js.executeScript("return document.body.scrollHeight");
            int scrollPosition = (int) (pageHeight * (0.2 + Math.random() * 0.6));
            
            js.executeScript("window.scrollTo(0, arguments[0]);", scrollPosition);
            humanPause(1000, 2000);
            
            // 다시 맨 위로 스크롤
            js.executeScript("window.scrollTo(0, 0);");
            humanPause(500, 1000);
            
        } catch (Exception e) {
            log.debug("자연스러운 스크롤 시뮬레이션 실패: {}", e.getMessage());
        }
    }
    
    /**
     * 마우스 움직임 시뮬레이션 (더 자연스러운 동작)
     */
    public void simulateMouseMovement(WebDriver driver) {
        try {
            Actions actions = new Actions(driver);
            
            // 랜덤한 마우스 움직임 시뮬레이션 (범위 제한)
            for (int i = 0; i < 3; i++) {
                int x = (int) (Math.random() * 400) + 50;  // 50-450 범위
                int y = (int) (Math.random() * 300) + 50;  // 50-350 범위
                
                try {
                    actions.moveByOffset(x, y).pause(Duration.ofMillis(200 + (long)(Math.random()*300))).perform();
                    humanPause(200, 500);
                } catch (Exception e) {
                    log.debug("마우스 움직임 실패 (범위 초과): {}", e.getMessage());
                    // 범위 초과 시 더 작은 움직임으로 재시도
                    try {
                        actions.moveByOffset(x/2, y/2).pause(Duration.ofMillis(200)).perform();
                    } catch (Exception ex) {
                        log.debug("마우스 움직임 재시도 실패: {}", ex.getMessage());
                    }
                }
            }
            
        } catch (Exception e) {
            log.debug("마우스 움직임 시뮬레이션 실패: {}", e.getMessage());
        }
    }
}
