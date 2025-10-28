package com.inventory.registration.service.bunjang;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.JavascriptExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * 번개장터 토큰 캡처 서비스
 * - 로그인 완료 후 x-bun-auth-token 추출
 * - localStorage, sessionStorage, 쿠키에서 토큰 검색
 */
@Component
@Slf4j
public class BunjangTokenCapturer {
    
    /**
     * 로그인 완료 후 토큰 캡처 (JavaScript 방식)
     * @param driver WebDriver 인스턴스
     * @return 추출된 토큰 또는 null
     */
    public String captureToken(WebDriver driver) {
        log.info("🔍 Starting token capture process (JavaScript method)...");
        
        try {
            // JavaScript 방식으로 토큰 캡처
            String token = captureTokenWithJavaScript(driver);
            if (token != null && isValidToken(token)) {
                log.info("✅ Token captured successfully: {}", maskToken(token));
                return token;
            }
            
            log.warn("❌ Token not found with any method");
            return null;
            
        } catch (Exception e) {
            log.error("❌ Token capture failed: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * JavaScript 방식으로 토큰 캡처 (fallback)
     */
    private String captureTokenWithJavaScript(WebDriver driver) {
        try {
            // 1. 페이지 로드 완료 대기
            waitForPageLoad(driver);
            
            // 2. localStorage에서 토큰 검색
            String token = searchInLocalStorage(driver);
            if (token != null) {
                log.info("✅ Token found in localStorage: {}", maskToken(token));
                return token;
            }
            
            // 3. sessionStorage에서 토큰 검색
            token = searchInSessionStorage(driver);
            if (token != null) {
                log.info("✅ Token found in sessionStorage: {}", maskToken(token));
                return token;
            }
            
            // 4. 쿠키에서 토큰 검색
            token = searchInCookies(driver);
            if (token != null) {
                log.info("✅ Token found in cookies: {}", maskToken(token));
                return token;
            }
            
            // 5. 네트워크 요청 헤더에서 토큰 검색 (DevTools 사용)
            token = searchInNetworkHeaders(driver);
            if (token != null) {
                log.info("✅ Token found in network headers: {}", maskToken(token));
                return token;
            }
            
            return null;
            
        } catch (Exception e) {
            log.error("❌ JavaScript token capture failed: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 페이지 로드 완료 대기
     */
    private void waitForPageLoad(WebDriver driver) {
        try {
            log.info("⏳ Waiting for page load completion...");
            Thread.sleep(3000); // 기본 대기
            
            // JavaScript로 페이지 로드 상태 확인
            JavascriptExecutor js = (JavascriptExecutor) driver;
            Boolean pageLoadComplete = (Boolean) js.executeScript("return document.readyState === 'complete'");
            
            if (pageLoadComplete) {
                log.info("✅ Page load completed");
            } else {
                log.warn("⚠️ Page load may not be complete, proceeding anyway");
            }
        } catch (Exception e) {
            log.warn("Page load check failed: {}", e.getMessage());
        }
    }
    
    /**
     * localStorage에서 토큰 검색
     */
    private String searchInLocalStorage(WebDriver driver) {
        try {
            log.info("🔍 Searching in localStorage...");
            JavascriptExecutor js = (JavascriptExecutor) driver;
            
            // localStorage의 모든 키 검색
            String script = """
                var keys = Object.keys(localStorage);
                for (var i = 0; i < keys.length; i++) {
                    var key = keys[i];
                    var value = localStorage.getItem(key);
                    if (key.toLowerCase().includes('token') || 
                        key.toLowerCase().includes('auth') ||
                        key.toLowerCase().includes('bun') ||
                        (value && value.length > 50 && value.includes('.'))) {
                        return value;
                    }
                }
                return null;
                """;
            
            String token = (String) js.executeScript(script);
            if (token != null && !token.isEmpty()) {
                log.info("Found potential token in localStorage");
                return token;
            }
            
            // 특정 키 이름으로 직접 검색
            String[] tokenKeys = {
                "x-bun-auth-token",
                "auth-token", 
                "token",
                "bun-token",
                "bunjang-token",
                "access-token",
                "bunjang_auth_token",
                "bun_auth_token",
                "authToken",
                "bunAuthToken"
            };
            
            for (String key : tokenKeys) {
                try {
                    String value = (String) js.executeScript("return localStorage.getItem('" + key + "');");
                    if (value != null && !value.isEmpty()) {
                        log.info("Found token with key '{}' in localStorage", key);
                        return value;
                    }
                } catch (Exception e) {
                    // 키가 존재하지 않는 경우 무시
                }
            }
            
        } catch (Exception e) {
            log.warn("localStorage search failed: {}", e.getMessage());
        }
        
        return null;
    }
    
    /**
     * sessionStorage에서 토큰 검색
     */
    private String searchInSessionStorage(WebDriver driver) {
        try {
            log.info("🔍 Searching in sessionStorage...");
            JavascriptExecutor js = (JavascriptExecutor) driver;
            
            // sessionStorage의 모든 키 검색
            String script = """
                var keys = Object.keys(sessionStorage);
                for (var i = 0; i < keys.length; i++) {
                    var key = keys[i];
                    var value = sessionStorage.getItem(key);
                    if (key.toLowerCase().includes('token') || 
                        key.toLowerCase().includes('auth') ||
                        key.toLowerCase().includes('bun') ||
                        (value && value.length > 50 && value.includes('.'))) {
                        return value;
                    }
                }
                return null;
                """;
            
            String token = (String) js.executeScript(script);
            if (token != null && !token.isEmpty()) {
                log.info("Found potential token in sessionStorage");
                return token;
            }
            
        } catch (Exception e) {
            log.warn("sessionStorage search failed: {}", e.getMessage());
        }
        
        return null;
    }
    
    /**
     * 쿠키에서 토큰 검색
     */
    private String searchInCookies(WebDriver driver) {
        try {
            log.info("🔍 Searching in cookies...");
            JavascriptExecutor js = (JavascriptExecutor) driver;
            
            // document.cookie에서 토큰 검색
            String script = """
                var cookies = document.cookie.split(';');
                for (var i = 0; i < cookies.length; i++) {
                    var cookie = cookies[i].trim();
                    var lowerCookie = cookie.toLowerCase();
                    if (lowerCookie.includes('token') || 
                        lowerCookie.includes('auth') ||
                        lowerCookie.includes('bun') ||
                        lowerCookie.includes('x-bun')) {
                        var value = cookie.split('=')[1];
                        if (value && value.length > 5) {
                            console.log('Found cookie:', cookie.substring(0, 20) + '...');
                            return value;
                        }
                    }
                }
                return null;
                """;
            
            String token = (String) js.executeScript(script);
            if (token != null && !token.isEmpty()) {
                log.info("Found potential token in cookies");
                return token;
            }
            
        } catch (Exception e) {
            log.warn("Cookie search failed: {}", e.getMessage());
        }
        
        return null;
    }
    
    /**
     * 네트워크 요청 헤더에서 토큰 검색 (DevTools 사용)
     */
    private String searchInNetworkHeaders(WebDriver driver) {
        try {
            log.info("🔍 Searching in network headers...");
            
            // DevTools를 사용하여 최근 요청의 헤더 검사
            JavascriptExecutor js = (JavascriptExecutor) driver;
            
            // Performance API를 사용하여 네트워크 요청 정보 수집
            String script = """
                return new Promise((resolve) => {
                    var observer = new PerformanceObserver((list) => {
                        var entries = list.getEntries();
                        for (var i = 0; i < entries.length; i++) {
                            var entry = entries[i];
                            if (entry.name && entry.name.includes('bunjang')) {
                                // 네트워크 요청이 감지되었지만 헤더는 직접 접근 불가
                                // 대신 현재 페이지의 모든 스크립트에서 토큰 검색
                                var scripts = document.getElementsByTagName('script');
                                for (var j = 0; j < scripts.length; j++) {
                                    var scriptContent = scripts[j].innerHTML;
                                    if (scriptContent.includes('x-bun-auth-token') || 
                                        scriptContent.includes('auth-token')) {
                                        var match = scriptContent.match(/['"]([A-Za-z0-9._-]{50,})['"]/);
                                        if (match) {
                                            resolve(match[1]);
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    });
                    observer.observe({entryTypes: ['resource']});
                    
                    // 5초 후 타임아웃
                    setTimeout(() => resolve(null), 5000);
                });
                """;
            
            String token = (String) js.executeScript(script);
            if (token != null && !token.isEmpty()) {
                log.info("Found potential token in network headers");
                return token;
            }
            
        } catch (Exception e) {
            log.warn("Network headers search failed: {}", e.getMessage());
        }
        
        return null;
    }
    
    /**
     * 토큰 마스킹 (보안을 위해 일부만 표시)
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 10) {
            return "***";
        }
        return token.substring(0, 8) + "..." + token.substring(token.length() - 8);
    }
    
    /**
     * 토큰 유효성 검사
     */
    public boolean isValidToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        
        log.info("🔍 Validating token: {}", maskToken(token));
        
        // JWT 토큰 형식 검사 (점으로 구분된 3부분)
        String[] parts = token.split("\\.");
        if (parts.length == 3) {
            log.info("✅ Token appears to be a valid JWT format");
            return true;
        }
        
        // 일반 토큰 형식 검사 (최소 길이 10자 이상)
        if (token.length() >= 10) {
            log.info("✅ Token appears to be a valid format (length: {})", token.length());
            return true;
        }
        
        // 쿠키에서 추출된 토큰의 경우 더 관대하게 검사
        if (token.length() >= 5 && (token.contains("-") || token.contains("_") || token.matches(".*[A-Za-z0-9].*"))) {
            log.info("✅ Token appears to be a valid cookie format (length: {})", token.length());
            return true;
        }
        
        log.warn("❌ Token format appears invalid (length: {}, content: {})", token.length(), maskToken(token));
        return false;
    }
}
