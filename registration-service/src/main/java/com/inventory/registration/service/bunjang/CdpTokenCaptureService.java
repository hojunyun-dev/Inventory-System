package com.inventory.registration.service.bunjang;

import com.example.common.dto.CookieEntry;
import com.example.common.dto.TokenBundle;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * CDP 기반 토큰 캡처 서비스
 * - Chrome for Testing과 호환되는 CDP 명령 사용
 * - Network.getAllCookies로 HttpOnly 쿠키 포함 토큰 캡처
 * - JavaScript 폴백 지원
 */
@Service
@Slf4j
public class CdpTokenCaptureService {

    @Value("${chrome.for-testing.path:~/chrome-for-testing/chrome-linux64/chrome}")
    private String chromeForTestingPath;

    @Value("${chrome.driver.path:~/chrome-for-testing/chromedriver-linux64/chromedriver}")
    private String chromeDriverPath;

    /**
     * CDP를 사용한 토큰 캡처
     */
    public TokenBundle captureTokenWithCDP(String platform, String sellUrl) {
        ChromeDriver driver = null;
        try {
            log.info("🔍 Starting CDP-based token capture for platform: {}", platform);
            
            // Chrome for Testing 설정
            ChromeOptions options = new ChromeOptions();
            options.setBinary(chromeForTestingPath);
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-blink-features=AutomationControlled");
            options.addArguments("--user-agent=Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Mobile Safari/537.36");
            
            // ChromeDriver 설정
            System.setProperty("webdriver.chrome.driver", chromeDriverPath);
            
            driver = new ChromeDriver(options);
            
            // CDP 명령으로 네트워크 모니터링 활성화
            driver.executeCdpCommand("Network.enable", Map.of("maxTotalBufferSize", 10_000_000));
            
            // 1차 쿠키 수집
            List<CookieEntry> cookies1 = getAllCookiesViaCDP(driver);
            log.info("📊 First cookie collection: {} cookies", cookies1.size());
            
            // 판매 페이지로 이동 (CSRF/세션 업데이트 트리거)
            driver.navigate().to(sellUrl);
            Thread.sleep(2000); // 페이지 로딩 대기
            
            // CSRF 토큰 추출 (JavaScript)
            String csrf = extractCsrfToken(driver);
            log.info("🔐 CSRF token extracted: {}", csrf != null ? "Found" : "Not found");
            
            // 2차 쿠키 수집 (업데이트된 쿠키)
            List<CookieEntry> cookies2 = getAllCookiesViaCDP(driver);
            log.info("📊 Second cookie collection: {} cookies", cookies2.size());
            
            // 더 많은 쿠키가 있는 것을 선택
            List<CookieEntry> finalCookies = cookies2.size() > cookies1.size() ? cookies2 : cookies1;
            
            // 만료 시간 계산 (8시간 후)
            Instant expiresAt = Instant.now().plusSeconds(8 * 3600);
            
            TokenBundle tokenBundle = new TokenBundle(platform, finalCookies, csrf, expiresAt);
            log.info("✅ CDP token capture completed: {} cookies, CSRF: {}", 
                finalCookies.size(), csrf != null ? "Yes" : "No");
            
            return tokenBundle;
            
        } catch (Exception e) {
            log.error("❌ CDP token capture failed: {}", e.getMessage(), e);
            return null;
        } finally {
            if (driver != null) {
                try {
                    driver.quit();
                } catch (Exception e) {
                    log.warn("Failed to quit driver: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * CDP 명령으로 모든 쿠키 가져오기
     */
    private List<CookieEntry> getAllCookiesViaCDP(ChromeDriver driver) {
        try {
            Map<String, Object> response = driver.executeCdpCommand("Network.getAllCookies", Map.of());
            List<Map<String, Object>> cookies = (List<Map<String, Object>>) response.get("cookies");
            
            if (cookies == null) {
                return new ArrayList<>();
            }
            
            List<CookieEntry> cookieEntries = new ArrayList<>();
            for (Map<String, Object> cookie : cookies) {
                CookieEntry entry = new CookieEntry(
                    (String) cookie.get("name"),
                    (String) cookie.get("value"),
                    (String) cookie.get("domain"),
                    (String) cookie.get("path"),
                    cookie.get("expires") != null ? ((Number) cookie.get("expires")).longValue() : null,
                    (Boolean) cookie.get("httpOnly"),
                    (Boolean) cookie.get("secure")
                );
                cookieEntries.add(entry);
            }
            
            return cookieEntries;
        } catch (Exception e) {
            log.error("Failed to get cookies via CDP: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * JavaScript로 CSRF 토큰 추출
     */
    private String extractCsrfToken(ChromeDriver driver) {
        try {
            String script = """
                return (document.querySelector('meta[name="csrf-token"]')?.content) || 
                       (document.querySelector('meta[name="x-csrf-token"]')?.content) || 
                       (window.__CSRF__ || null);
                """;
            
            Object result = driver.executeScript(script);
            return result != null ? result.toString() : null;
        } catch (Exception e) {
            log.warn("Failed to extract CSRF token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * JavaScript 폴백 토큰 캡처
     */
    public TokenBundle captureTokenWithJavaScript(String platform, String sellUrl) {
        ChromeDriver driver = null;
        try {
            log.info("🔍 Starting JavaScript-based token capture for platform: {}", platform);
            
            ChromeOptions options = new ChromeOptions();
            options.setBinary(chromeForTestingPath);
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-blink-features=AutomationControlled");
            
            System.setProperty("webdriver.chrome.driver", chromeDriverPath);
            driver = new ChromeDriver(options);
            
            driver.navigate().to(sellUrl);
            Thread.sleep(2000);
            
            // JavaScript로 토큰 검색
            String script = """
                const tokens = [];
                
                // localStorage 검색
                for (let i = 0; i < localStorage.length; i++) {
                    const key = localStorage.key(i);
                    const value = localStorage.getItem(key);
                    if (key.toLowerCase().includes('token') || key.toLowerCase().includes('auth')) {
                        tokens.push({source: 'localStorage', key: key, value: value});
                    }
                }
                
                // sessionStorage 검색
                for (let i = 0; i < sessionStorage.length; i++) {
                    const key = sessionStorage.key(i);
                    const value = sessionStorage.getItem(key);
                    if (key.toLowerCase().includes('token') || key.toLowerCase().includes('auth')) {
                        tokens.push({source: 'sessionStorage', key: key, value: value});
                    }
                }
                
                // 쿠키 검색
                const cookies = document.cookie.split(';');
                cookies.forEach(cookie => {
                    const [key, value] = cookie.trim().split('=');
                    if (key && (key.toLowerCase().includes('token') || key.toLowerCase().includes('auth'))) {
                        tokens.push({source: 'cookie', key: key, value: value});
                    }
                });
                
                return tokens;
                """;
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> tokenResults = (List<Map<String, Object>>) driver.executeScript(script);
            
            List<CookieEntry> cookies = new ArrayList<>();
            String csrf = null;
            
            for (Map<String, Object> token : tokenResults) {
                String source = (String) token.get("source");
                String key = (String) token.get("key");
                String value = (String) token.get("value");
                
                if ("cookie".equals(source)) {
                    cookies.add(new CookieEntry(key, value));
                } else if (key.toLowerCase().contains("csrf")) {
                    csrf = value;
                }
            }
            
            Instant expiresAt = Instant.now().plusSeconds(8 * 3600);
            TokenBundle tokenBundle = new TokenBundle(platform, cookies, csrf, expiresAt);
            
            log.info("✅ JavaScript token capture completed: {} cookies, CSRF: {}", 
                cookies.size(), csrf != null ? "Yes" : "No");
            
            return tokenBundle;
            
        } catch (Exception e) {
            log.error("❌ JavaScript token capture failed: {}", e.getMessage(), e);
            return null;
        } finally {
            if (driver != null) {
                try {
                    driver.quit();
                } catch (Exception e) {
                    log.warn("Failed to quit driver: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * 통합 토큰 캡처 (CDP 우선, JavaScript 폴백)
     */
    public TokenBundle captureToken(String platform, String sellUrl) {
        log.info("🚀 Starting integrated token capture for platform: {}", platform);
        
        // 1. CDP 방식 시도
        TokenBundle cdpResult = captureTokenWithCDP(platform, sellUrl);
        if (cdpResult != null && !cdpResult.cookies.isEmpty()) {
            log.info("✅ CDP token capture successful");
            return cdpResult;
        }
        
        // 2. JavaScript 폴백 시도
        log.info("🔄 CDP failed, trying JavaScript fallback...");
        TokenBundle jsResult = captureTokenWithJavaScript(platform, sellUrl);
        if (jsResult != null && !jsResult.cookies.isEmpty()) {
            log.info("✅ JavaScript token capture successful");
            return jsResult;
        }
        
        log.error("❌ All token capture methods failed");
        return null;
    }
}


