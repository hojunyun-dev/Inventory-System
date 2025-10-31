package com.inventory.registration.config;

// WebDriverManager 제거 - Selenium Manager 사용
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import jakarta.annotation.PreDestroy;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

@Configuration
@ConditionalOnProperty(name = "selenium.enabled", havingValue = "true")
@Slf4j
public class WebDriverConfig {

    @Autowired
    private AutomationProperties automationProperties;

    @org.springframework.context.annotation.Lazy
    @Bean
    @Primary
    @org.springframework.context.annotation.Scope("prototype")
    public WebDriver webDriver() {
        log.info("Initializing WebDriver with headless={}, timeout={}ms", 
            automationProperties.getBrowser().isHeadless(), 
            automationProperties.getBrowser().getTimeout());
        
        ChromeOptions options = new ChromeOptions();
        
        // 시스템 Chrome 사용 (Selenium Manager가 자동 관리)
        // options.setBinary("/home/code/chrome-for-testing/chrome-linux64/chrome");
        
        // 기본 옵션 설정 (헤드리스 모드용)
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-plugins");
        options.addArguments("--disable-images");
        options.addArguments("--no-first-run");
        options.addArguments("--no-default-browser-check");
        options.addArguments("--lang=ko-KR");
        options.addArguments("--disable-background-timer-throttling");
        options.addArguments("--disable-backgrounding-occluded-windows");
        options.addArguments("--disable-renderer-backgrounding");
        options.addArguments("--disable-features=TranslateUI");
        options.addArguments("--disable-ipc-flooding-protection");
        // 안정성 저하 가능 플래그 제거 (충돌 방지)
        // options.addArguments("--disable-web-security");
        // options.addArguments("--allow-running-insecure-content");
        // options.addArguments("--disable-features=VizDisplayCompositor");
        // options.addArguments("--remote-debugging-port=0");
        // options.addArguments("--disable-background-timer-throttling");
        // options.addArguments("--disable-backgrounding-occluded-windows");
        // options.addArguments("--disable-renderer-backgrounding");
        // options.addArguments("--disable-features=TranslateUI");
        // options.addArguments("--disable-ipc-flooding-protection");
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        
        // 봇 탐지 우회 옵션 (팀원 코드에서 가져온 개선사항)
        options.setExperimentalOption("excludeSwitches", java.util.Collections.singletonList("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);
        options.addArguments("--disable-blink-features=AutomationControlled");
        
        // 창 크기 설정
        String windowSize = automationProperties.getBrowser().getWindowSize();
        if (windowSize != null && !windowSize.isEmpty()) {
            options.addArguments("--window-size=" + windowSize);
        }
        
        // 헤드리스 모드 설정
        if (automationProperties.getBrowser().isHeadless()) {
            options.addArguments("--headless");
        }
        
        // 원격 WebDriver 사용 여부
        String remoteUrl = automationProperties.getBrowser().getRemoteUrl();
        if (remoteUrl == null || remoteUrl.isEmpty()) {
            String envUrl = System.getenv("AUTOMATION_BROWSER_REMOTE_URL");
            if (envUrl != null && !envUrl.isEmpty()) {
                remoteUrl = envUrl;
            }
        }
        if (remoteUrl != null && !remoteUrl.isEmpty()) {
            try {
                log.info("Using remote WebDriver at: {}", remoteUrl);
                return new RemoteWebDriver(new URL(remoteUrl), options);
            } catch (MalformedURLException e) {
                log.error("Invalid remote URL: {}", remoteUrl, e);
                throw new RuntimeException("Invalid remote WebDriver URL", e);
            }
        } else {
            // 로컬 Chrome WebDriver 사용 (Selenium Manager 자동 관리)
            // 계정별 프로필 디렉토리 설정 (세션 재사용을 위해 - 고정 디렉토리)
            String profileBase = System.getProperty("automation.profile.base", System.getProperty("user.home") + "/.selenium-profiles");
            String profileDir = profileBase + "/bunjang-session-" + System.currentTimeMillis(); // 고유한 프로필로 충돌 방지
            options.addArguments("--user-data-dir=" + profileDir);
            options.addArguments("--profile-directory=Default");

            ChromeDriver driver = new ChromeDriver(options);
            
            // 타임아웃 설정
            long timeout = automationProperties.getBrowser().getTimeout();
            driver.manage().timeouts().implicitlyWait(Duration.ofMillis(timeout));
            driver.manage().timeouts().pageLoadTimeout(Duration.ofMillis(timeout));
            
            return driver;
        }
    }

    @PreDestroy
    public void cleanup() {
        log.info("Cleaning up WebDriver resources");
        // WebDriver 정리는 각 서비스에서 수행
    }
}
