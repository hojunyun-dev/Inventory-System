package com.inventory.registration.config;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import jakarta.annotation.PreDestroy;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

@Configuration
@Slf4j
public class WebDriverConfig {

    @Value("${automation.browser.headless:true}")
    private boolean headless;

    @Value("${automation.browser.timeout:30000}")
    private long timeout;

    @Value("${automation.browser.window-size:1920,1080}")
    private String windowSize;

    @Value("${automation.browser.remote-url:}")
    private String remoteUrl;

    @Bean
    @Primary
    public WebDriver webDriver() {
        log.info("Initializing WebDriver with headless={}, timeout={}ms", headless, timeout);
        
        ChromeOptions options = new ChromeOptions();
        
        // 기본 옵션 설정
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-plugins");
        options.addArguments("--disable-images"); // 이미지 로딩 비활성화로 속도 향상
        // options.addArguments("--disable-javascript"); // 번개장터는 JavaScript 필요하므로 비활성화
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        
        // 창 크기 설정
        if (windowSize != null && !windowSize.isEmpty()) {
            options.addArguments("--window-size=" + windowSize);
        }
        
        // 헤드리스 모드 설정
        if (headless) {
            options.addArguments("--headless");
        }
        
        // 원격 WebDriver 사용 여부
        if (remoteUrl != null && !remoteUrl.isEmpty()) {
            try {
                log.info("Using remote WebDriver at: {}", remoteUrl);
                return new RemoteWebDriver(new URL(remoteUrl), options);
            } catch (MalformedURLException e) {
                log.error("Invalid remote URL: {}", remoteUrl, e);
                throw new RuntimeException("Invalid remote WebDriver URL", e);
            }
        } else {
            // 로컬 Chrome WebDriver 사용
            WebDriverManager.chromedriver().setup();
            ChromeDriver driver = new ChromeDriver(options);
            
            // 타임아웃 설정
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
