package com.inventory.registration.service.bunjang;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.PageLoadStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
// WebDriverManager 제거 - Selenium Manager 사용

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Set;

/**
 * 번개장터 WebDriver 관리 클래스
 * - WebDriver 생성 및 설정
 * - 브라우저 옵션 관리
 * - 세션 관리
 */
@Component
@Slf4j
public class BunjangWebDriverManager {
    
    @Autowired
    private BunjangTokenCapturer tokenCapturer;
    
    @Value("${automation.browser.headless}")
    private Boolean headless;
    
    @Value("${automation.proxy.enabled:false}")
    private Boolean proxyEnabled;
    
    @Value("${automation.proxy.host:}")
    private String proxyHost;
    
    @Value("${automation.proxy.port:8080}")
    private Integer proxyPort;
    
    // 프로필 경로 - 세션 유지를 위해 고정된 경로 사용
    private static final String PERSISTENT_PROFILE_PATH = "/home/code/.selenium-profiles/bunjang-profile";
    
    private WebDriver webDriver;
    
    /**
     * WebDriver 인스턴스 생성 및 설정
     */
    public WebDriver createWebDriver() {
        log.info("Creating new WebDriver instance with persistent profile...");
        
        try {
            // ChromeDriver 자동 관리
            // Selenium Manager가 자동으로 ChromeDriver 관리
            log.info("ChromeDriver setup completed");
            
            // Chrome 옵션 설정
            ChromeOptions options = createChromeOptions();
            
            // WebDriver 생성 - 원격 Selenium Grid 사용
            String remoteUrl = System.getenv("AUTOMATION_BROWSER_REMOTE_URL");
            if (remoteUrl == null || remoteUrl.isEmpty()) {
                remoteUrl = "http://selenium:4444/wd/hub";
            }
            log.info("Using remote WebDriver at: {}", remoteUrl);
            webDriver = new RemoteWebDriver(new URL(remoteUrl), options);
            log.info("RemoteWebDriver instance created successfully");
            
            // 명시적 대기 설정
            setupTimeouts(webDriver);
            
            // 브라우저 생성 후 안정성 확인 (속도 최적화)
            log.info("🔍 브라우저 안정성 확인 중...");
            Thread.sleep(1000); // 1초 대기하여 브라우저 안정화
            
            // 브라우저 세션 유효성 검사
            if (!isSessionValid(webDriver)) {
                log.error("❌ 브라우저 세션이 불안정합니다. 재생성 시도...");
                webDriver.quit();
                Thread.sleep(2000);
                webDriver = new RemoteWebDriver(new URL(remoteUrl), options);
                setupTimeouts(webDriver);
                Thread.sleep(1000);
            }
            
            // 브라우저 생성 후 번개장터 홈 페이지로 이동
            navigateToBunjang(webDriver);
            
            // 자동화 감지 우회 JavaScript 실행
            executeAntiDetectionScript(webDriver);
            
            // 토큰 캡처를 위한 JavaScript 후킹 스니펫 미리 주입
            injectTokenHookingScript(webDriver);
            
            log.info("✅ WebDriver created successfully with enhanced session management");
            return webDriver;
            
        } catch (Exception e) {
            log.error("Failed to create WebDriver: {}", e.getMessage(), e);
            if (webDriver != null) {
                try {
                    webDriver.quit();
                } catch (Exception ex) {
                    log.warn("Failed to quit WebDriver during cleanup: {}", ex.getMessage());
                }
                webDriver = null;
            }
            throw new RuntimeException("WebDriver creation failed", e);
        }
    }
    
    /**
     * Chrome 옵션 설정
     */
    private ChromeOptions createChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        
        // 🚀 속도 최적화 (EAGER 로딩 전략)
        options.setPageLoadStrategy(PageLoadStrategy.EAGER);
        options.addArguments("--lang=ko-KR");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--start-maximized");
        
        // 브라우저 안정성 강화 (세션 유지 우선)
        options.addArguments("--no-sandbox");  // sandbox 비활성화 (안정성 우선)
        options.addArguments("--disable-dev-shm-usage");  // 공유 메모리 사용량 제한
        options.addArguments("--disable-gpu");  // GPU 비활성화로 메모리 절약
        options.addArguments("--disable-software-rasterizer");  // 소프트웨어 래스터라이저 비활성화
        options.addArguments("--disable-web-security");  // 웹 보안 비활성화
        options.addArguments("--disable-features=VizDisplayCompositor");  // 디스플레이 컴포지터 비활성화
        options.addArguments("--disable-extensions");  // 확장 프로그램 비활성화
        options.addArguments("--disable-plugins");  // 플러그인 비활성화
        // 이미지 로딩 활성화 (수동 로그인을 위해 필요)
        options.addArguments("--disable-default-apps");  // 기본 앱 비활성화
        options.addArguments("--disable-background-timer-throttling");  // 백그라운드 타이머 스로틀링 비활성화
        options.addArguments("--disable-backgrounding-occluded-windows");  // 가려진 창 백그라운드 처리 비활성화
        options.addArguments("--disable-renderer-backgrounding");  // 렌더러 백그라운드 처리 비활성화
        options.addArguments("--disable-features=TranslateUI");  // 번역 UI 비활성화
        
        // 🔧 브라우저 세션 유지 강화 (로그인 후 닫히지 않도록)
        options.addArguments("--disable-session-crashed-bubble");  // 세션 크래시 버블 비활성화
        options.addArguments("--disable-infobars");  // 정보 바 비활성화
        options.addArguments("--disable-hang-monitor");  // 행 모니터 비활성화
        options.addArguments("--disable-prompt-on-repost");  // 재전송 프롬프트 비활성화
        options.addArguments("--disable-background-downloads");  // 백그라운드 다운로드 비활성화
        options.addArguments("--disable-add-to-shelf");  // 선반 추가 비활성화
        options.addArguments("--keep-alive-for-test");  // 테스트용 세션 유지
        options.addArguments("--disable-blink-features=AutomationControlled");  // 자동화 감지 비활성화
        options.addArguments("--disable-features=VizDisplayCompositor");  // 디스플레이 컴포지터 비활성화
        options.addArguments("--no-first-run");  // 첫 실행 설정 비활성화
        options.addArguments("--no-default-browser-check");  // 기본 브라우저 확인 비활성화
        options.addArguments("--disable-component-update");  // 컴포넌트 업데이트 비활성화
        options.addArguments("--disable-domain-reliability");  // 도메인 신뢰성 비활성화
        options.addArguments("--disable-ipc-flooding-protection");  // IPC 플러딩 보호 비활성화
        options.addArguments("--disable-hang-monitor");  // 행 모니터 비활성화
        options.addArguments("--disable-prompt-on-repost");  // 재전송 프롬프트 비활성화
        options.addArguments("--disable-background-downloads");  // 백그라운드 다운로드 비활성화
        options.addArguments("--disable-add-to-shelf");  // 선반 추가 비활성화
        options.addArguments("--disable-client-side-phishing-detection");  // 클라이언트 사이드 피싱 감지 비활성화
        options.addArguments("--disable-component-update");  // 컴포넌트 업데이트 비활성화
        options.addArguments("--disable-domain-reliability");  // 도메인 신뢰성 비활성화
        options.addArguments("--disable-sync");  // 동기화 비활성화
        options.addArguments("--disable-sync-preferences");  // 동기화 설정 비활성화
        options.addArguments("--disable-sync-app-list");  // 동기화 앱 목록 비활성화
        options.addArguments("--disable-sync-app-settings");  // 동기화 앱 설정 비활성화
        options.addArguments("--disable-sync-extension-settings");  // 동기화 확장 설정 비활성화
        options.addArguments("--disable-sync-search-engines");  // 동기화 검색 엔진 비활성화
        options.addArguments("--disable-sync-themes");  // 동기화 테마 비활성화
        options.addArguments("--disable-sync-typed-urls");  // 동기화 타이핑 URL 비활성화
        options.addArguments("--disable-sync-wifi-credentials");  // 동기화 WiFi 자격 증명 비활성화
        options.addArguments("--disable-mobile-emulation");  // 모바일 에뮬레이션 비활성화
        options.addArguments("--disable-device-emulation");  // 디바이스 에뮬레이션 비활성화
        options.addArguments("--force-device-scale-factor=1");  // 디바이스 스케일 팩터 고정
        options.addArguments("--disable-accelerated-2d-canvas");  // 가속 2D 캔버스 비활성화
        options.addArguments("--disable-accelerated-jpeg-decoding");  // 가속 JPEG 디코딩 비활성화
        options.addArguments("--disable-accelerated-mjpeg-decode");  // 가속 MJPEG 디코딩 비활성화
        options.addArguments("--disable-accelerated-video-decode");  // 가속 비디오 디코딩 비활성화
        options.addArguments("--disable-crash-reporter");  // 크래시 리포터 비활성화
        options.addArguments("--disable-logging");  // 로깅 비활성화
        options.addArguments("--log-level=0");  // 로그 레벨 0 (최소)
        options.addArguments("--silent");  // 무음 모드
        options.addArguments("--no-service-autorun");  // 서비스 자동 실행 비활성화
        options.addArguments("--password-store=basic");  // 기본 비밀번호 저장소
        options.addArguments("--use-mock-keychain");  // 모의 키체인 사용
        options.addArguments("--test-type=webdriver");  // WebDriver 테스트 타입
        options.addArguments("--allow-pre-commit-input");  // 사전 커밋 입력 허용
        options.addArguments("--disable-gpu-compositing");  // GPU 컴포지팅 비활성화
        options.addArguments("--disable-blink-features=AutomationControlled");  // 자동화 제어 기능 비활성화
        options.addArguments("--remote-debugging-port=0");  // 원격 디버깅 포트 비활성화
        options.addArguments("--disable-software-rasterizer");
        options.addArguments("--disable-background-timer-throttling");
        options.addArguments("--disable-backgrounding-occluded-windows");
        options.addArguments("--disable-renderer-backgrounding");
        options.addArguments("--disable-features=TranslateUI");
        options.addArguments("--disable-ipc-flooding-protection");
        
        // 메모리 관리 최적화 (보수적)
        options.addArguments("--max_old_space_size=1024");  // 메모리 사용량 보수적 설정
        options.addArguments("--memory-pressure-off");  // 메모리 압박 해제
        options.addArguments("--disable-background-timer-throttling");  // 백그라운드 타이머 스로틀링 비활성화
        options.addArguments("--disable-background-networking");
        options.addArguments("--disable-default-apps");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-sync");
        
        // 프로세스 안정성 (sandbox 유지하되 메모리 최적화)
        // --single-process 옵션 제거 (브라우저 안정성 문제 원인)
        options.addArguments("--disable-background-networking");
        options.addArguments("--disable-background-timer-throttling");
        
        // 추가 안정성 옵션
        options.addArguments("--disable-crash-reporter");
        options.addArguments("--disable-logging");
        options.addArguments("--disable-web-security");
        options.addArguments("--disable-features=VizDisplayCompositor");
        options.addArguments("--disable-accelerated-2d-canvas");
        options.addArguments("--disable-accelerated-jpeg-decoding");
        options.addArguments("--disable-accelerated-mjpeg-decode");
        options.addArguments("--disable-accelerated-video-decode");
        
        // 전체화면 모드는 kiosk 대신 일반 창으로 시작
        
        // 세션 유지 옵션 (원격 WebDriver 사용 시 프로필 경로 옵션 제거 - 충돌 방지)
        options.addArguments("--disable-session-crashed-bubble"); // 세션 크래시 버블 비활성화
        options.addArguments("--disable-infobars"); // 정보 바 비활성화
        options.addArguments("--disable-features=VizDisplayCompositor"); // 디스플레이 컴포지터 비활성화
        
        // 🩹 팝업/창전환 허용(특히 headless)
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-popup-blocking");
        
        // 🩹 탐지 회피 (강화된 설정)
        options.setExperimentalOption("excludeSwitches", java.util.List.of("enable-automation", "enable-logging"));
        options.setExperimentalOption("useAutomationExtension", false);
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-features=VizDisplayCompositor");
        
        // 추가 자동화 감지 우회 옵션
        options.addArguments("--disable-web-security");
        options.addArguments("--disable-features=VizDisplayCompositor,TranslateUI");
        options.addArguments("--disable-ipc-flooding-protection");
        options.addArguments("--disable-renderer-backgrounding");
        options.addArguments("--disable-backgrounding-occluded-windows");
        options.addArguments("--disable-client-side-phishing-detection");
        options.addArguments("--disable-sync");
        options.addArguments("--disable-default-apps");
        options.addArguments("--disable-extensions");
        options.addArguments("--no-first-run");
        options.addArguments("--no-default-browser-check");
        options.addArguments("--disable-component-update");
        options.addArguments("--disable-domain-reliability");
        options.addArguments("--disable-features=TranslateUI");
        options.addArguments("--disable-background-networking");
        options.addArguments("--disable-background-timer-throttling");
        options.addArguments("--disable-renderer-backgrounding");
        options.addArguments("--disable-backgrounding-occluded-windows");
        options.addArguments("--disable-mobile-emulation");
        options.addArguments("--disable-device-emulation");
        options.addArguments("--force-device-scale-factor=1");
        options.addArguments("--disable-hang-monitor");
        options.addArguments("--disable-prompt-on-repost");
        options.addArguments("--disable-background-downloads");
        options.addArguments("--disable-add-to-shelf");
        options.addArguments("--disable-client-side-phishing-detection");
        options.addArguments("--disable-component-update");
        options.addArguments("--disable-domain-reliability");
        options.addArguments("--disable-features=TranslateUI");
        options.addArguments("--disable-sync-preferences");
        options.addArguments("--disable-sync-app-list");
        options.addArguments("--disable-sync-app-settings");
        options.addArguments("--disable-sync-extension-settings");
        options.addArguments("--disable-sync-search-engines");
        options.addArguments("--disable-sync-themes");
        options.addArguments("--disable-sync-typed-urls");
        options.addArguments("--disable-sync-wifi-credentials");
        
        // 🩹 UA/플랫폼은 옵션에서 고정 (JS로 또 바꾸지 않음) - 랜덤 User-Agent 사용
        String randomUserAgent = getRandomUserAgent();
        options.addArguments("--user-agent=" + randomUserAgent);
        log.info("✅ Using User-Agent: {}", randomUserAgent);
        
        // 안정성
        options.addArguments("--disable-background-networking");
        options.addArguments("--disable-background-timer-throttling");
        options.addArguments("--disable-renderer-backgrounding");
        options.addArguments("--disable-backgrounding-occluded-windows");
        options.addArguments("--disable-mobile-emulation");
        options.addArguments("--disable-device-emulation");
        options.addArguments("--force-device-scale-factor=1");
        options.addArguments("--disable-features=VizDisplayCompositor");
        
        if (headless != null && headless) {
            options.addArguments("--headless=new");
        }
        
        // 프록시 설정 (활성화된 경우)
        if (proxyEnabled != null && proxyEnabled && proxyHost != null && !proxyHost.isEmpty()) {
            options.addArguments("--proxy-server=http://" + proxyHost + ":" + proxyPort);
            log.info("✅ Proxy enabled: {}:{}", proxyHost, proxyPort);
        }
        
        return options;
    }
    
    /**
     * 타임아웃 설정 (초고속 응답 최적화)
     */
    private void setupTimeouts(WebDriver driver) {
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2)); // 요소 탐색 대기 시간 단축 (15초 → 2초)
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(10)); // 페이지 로드 타임아웃 단축 (120초 → 10초)
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(5)); // 스크립트 타임아웃 단축 (60초 → 5초)
        log.info("✅ WebDriver 타임아웃 설정 완료 (초고속 응답 최적화)");
    }
    
    /**
     * 번개장터 홈페이지로 이동 (초고속 버전)
     */
    private void navigateToBunjang(WebDriver driver) {
        log.info("🚀 초고속 번개장터 홈으로 이동...");
        
        try {
            // 🚀 초고속 번개장터 홈으로 이동
            driver.get("https://www.bunjang.co.kr?desktop=1&force_desktop=true");
            Thread.sleep(500);  // 2초 → 0.5초로 단축
            
            String currentUrl = driver.getCurrentUrl();
            log.info("Final URL: {}", currentUrl);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("Navigation failed: {}", e.getMessage());
        }
    }
    
    /**
     * 자동화 감지 우회 JavaScript 실행
     */
    private void executeAntiDetectionScript(WebDriver driver) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            
                    // 강화된 자동화 감지 우회 스크립트
                    String antiDetectionScript = """
                        // WebDriver 속성 완전 제거
                        Object.defineProperty(navigator, 'webdriver', {
                            get: () => undefined,
                            configurable: true
                        });
                        
                        // Chrome 런타임 속성 추가
                        Object.defineProperty(navigator, 'plugins', {
                            get: () => [1, 2, 3, 4, 5],
                            configurable: true
                        });
                        
                        // 언어 설정
                        Object.defineProperty(navigator, 'languages', {
                            get: () => ['ko-KR', 'ko', 'en-US', 'en'],
                            configurable: true
                        });
                        
                        // 플랫폼 설정
                        Object.defineProperty(navigator, 'platform', {
                            get: () => 'Win32',
                            configurable: true
                        });
                        
                        // User Agent 정규화
                        Object.defineProperty(navigator, 'userAgent', {
                            get: () => 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
                            configurable: true
                        });
                        
                        // 자동화 관련 속성 완전 제거
                        delete window.cdc_adoQpoasnfa76pfcZLmcfl_Array;
                        delete window.cdc_adoQpoasnfa76pfcZLmcfl_Promise;
                        delete window.cdc_adoQpoasnfa76pfcZLmcfl_Symbol;
                        delete window.cdc_adoQpoasnfa76pfcZLmcfl_Object;
                        
                        // Chrome DevTools 관련 속성 제거
                        if (window.chrome && window.chrome.runtime) {
                            delete window.chrome.runtime.onConnect;
                            delete window.chrome.runtime.onMessage;
                            delete window.chrome.runtime.onConnectExternal;
                            delete window.chrome.runtime.onMessageExternal;
                        }
                        
                        // 추가 자동화 감지 우회
                        Object.defineProperty(navigator, 'permissions', {
                            get: () => ({
                                query: () => Promise.resolve({state: 'granted'})
                            }),
                            configurable: true
                        });
                        
                        // 스크린 속성 정규화
                        Object.defineProperty(screen, 'width', {
                            get: () => 1920,
                            configurable: true
                        });
                        Object.defineProperty(screen, 'height', {
                            get: () => 1080,
                            configurable: true
                        });
                        """;
            
            js.executeScript(antiDetectionScript);
            log.info("✅ Enhanced anti-detect applied");
        } catch (Exception e) {
            log.warn("anti-detect script failed: {}", e.getMessage());
        }
    }
    
    /**
     * WebDriver 인스턴스 반환
     */
    public WebDriver getWebDriver() {
        if (webDriver == null) {
            return createWebDriver();
        }
        return webDriver;
    }
    
    /**
     * WebDriver 세션 유효성 검사 및 자동 복구 (메모리 모니터링 포함)
     */
    public boolean isSessionValid(WebDriver driver) {
        try {
            if (driver == null) {
                log.warn("Driver is null, recreating...");
                return false;
            }
            
            // 메모리 사용량 체크
            checkMemoryUsage();
            
            // 현재 URL 확인
            String currentUrl = driver.getCurrentUrl();
            if (currentUrl == null || currentUrl.isEmpty()) {
                log.warn("Current URL is null or empty, session invalid");
                return false;
            }
            
            // JavaScript 실행 테스트
            JavascriptExecutor js = (JavascriptExecutor) driver;
            String title = (String) js.executeScript("return document.title;");
            
            if (title == null) {
                log.warn("JavaScript execution failed, session invalid");
                return false;
            }
            
            log.debug("Session valid - URL: {}, Title: {}", currentUrl, title);
            return true;
            
        } catch (WebDriverException e) {
            log.error("🚨 WebDriver session died: {}", e.getMessage());
            
            // 메모리 부족으로 인한 브라우저 종료 감지
            if (e.getMessage().contains("remote browser") || 
                e.getMessage().contains("died") ||
                e.getMessage().contains("invalid session")) {
                log.error("💾 브라우저가 메모리 부족으로 종료된 것으로 추정됩니다");
                log.info("🔄 Attempting automatic session recovery...");
                return false; // false를 반환하여 상위에서 재생성하도록 함
            }
            
            return false;
        } catch (Exception e) {
            log.error("Unexpected error during session validation: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 메모리 사용량 체크
     */
    private void checkMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        
        double memoryUsagePercent = (double) usedMemory / maxMemory * 100;
        
        if (memoryUsagePercent > 80) {
            log.warn("⚠️ 메모리 사용량 높음: {:.1f}% ({}/{} MB)", 
                memoryUsagePercent, usedMemory / 1024 / 1024, maxMemory / 1024 / 1024);
        } else {
            log.debug("💾 메모리 사용량: {:.1f}% ({}/{} MB)", 
                memoryUsagePercent, usedMemory / 1024 / 1024, maxMemory / 1024 / 1024);
        }
    }

    /**
     * WebDriver 자동 복구
     */
    public WebDriver recoverWebDriver() {
        log.info("🔄 Starting WebDriver recovery process...");
        
        try {
            // 기존 WebDriver 정리
            if (webDriver != null) {
                try {
                    webDriver.quit();
                } catch (Exception e) {
                    log.debug("Error quitting old driver: {}", e.getMessage());
                }
                webDriver = null;
            }
            
            // 새 WebDriver 생성
            Thread.sleep(2000); // 잠시 대기
            WebDriver newDriver = createWebDriver();
            
            log.info("✅ WebDriver recovery completed successfully");
            return newDriver;
            
        } catch (Exception e) {
            log.error("❌ WebDriver recovery failed: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * WebDriver 종료
     */
    public void quitWebDriver() {
        if (webDriver != null) {
            try {
                webDriver.quit();
                log.info("WebDriver 종료 완료");
            } catch (Exception e) {
                log.warn("WebDriver 종료 중 오류: {}", e.getMessage());
            } finally {
                webDriver = null;
            }
        }
    }
    
    /**
     * WebDriverWait 인스턴스 생성
     */
    public WebDriverWait createWebDriverWait(WebDriver driver, long seconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(seconds));
    }
    
    /**
     * 새창 전환 유틸 (LoginHandler/FormHandler에서 재사용)
     */
    public boolean switchToNewWindowIfOpened(WebDriver d, int waitSec) {
        String root = d.getWindowHandle();
        long end = System.currentTimeMillis() + waitSec * 1000L;
        while (System.currentTimeMillis() < end) {
            Set<String> hs = d.getWindowHandles();
            if (hs.size() > 1) {
                for (String h : hs) if (!h.equals(root)) { 
                    d.switchTo().window(h); 
                    return true; 
                }
            }
            try { 
                Thread.sleep(120); 
            } catch (InterruptedException ignored) { 
                Thread.currentThread().interrupt();
            }
        }
        return false;
    }
    
    /**
     * 랜덤 User-Agent 생성
     */
    private String getRandomUserAgent() {
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
     * 토큰 캡처를 위한 JavaScript 후킹 스니펫 주입
     */
    private void injectTokenHookingScript(WebDriver driver) {
        try {
            log.info("🔧 Injecting token hooking script at page load...");
            tokenCapturer.injectTokenHookingScript(driver);
        } catch (Exception e) {
            log.warn("⚠️ Failed to inject token hooking script: {}", e.getMessage());
        }
    }
}
