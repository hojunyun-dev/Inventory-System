package com.inventory.registration.service.bunjang;

import com.example.common.dto.CookieEntry;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.devtools.v128.network.Network;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;


/**
 * 번개장터 토큰 캡처 서비스
 * - 로그인 완료 후 x-bun-auth-token 추출
 * - localStorage, sessionStorage, 쿠키에서 토큰 검색
 */
@Component
@Slf4j
public class BunjangTokenCapturer {
    
    // x-bun-auth-token 패턴: 32자리 hex
    private static final Pattern AUTH_TOKEN_PATTERN = Pattern.compile("^[0-9a-f]{32}$", Pattern.CASE_INSENSITIVE);
    
    // CSRF 패턴: 문자-숫자 조합 (예: yGc027-1761629973)
    private static final Pattern CSRF_PATTERN = Pattern.compile("^[A-Za-z0-9_-]+-\\d+$");
    
    // 캡처된 토큰 저장
    private volatile String capturedAuthToken = null;
    
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
     * x-bun-auth-token 추출 (JavaScript 후킹 방식)
     * @param driver WebDriver 인스턴스
     * @return x-bun-auth-token
     */
    public String captureAuthToken(WebDriver driver) {
        log.info("🔍 Starting x-bun-auth-token capture process (JavaScript hooking)...");
        
        try {
            // 1. 로그인 완료 3초 후 토큰 추출 (스니펫은 이미 페이지 로드 시 주입됨)
            log.info("⏳ Waiting 3 seconds after login completion...");
            Thread.sleep(3000);
            
            // 2. 폴링 방식으로 토큰 캡처 (최대 15초)
            String authToken = pollForCapturedToken(driver);
            if (authToken != null && isValidAuthToken(authToken)) {
                log.info("✅ x-bun-auth-token captured via JavaScript hooking: {}", maskToken(authToken));
                return authToken;
            }
            
            log.warn("❌ x-bun-auth-token not found with JavaScript hooking method");
            return null;
            
        } catch (Exception e) {
            log.error("❌ x-bun-auth-token capture failed: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * CDP Network 리스너로 x-bun-auth-token 실시간 캡처 (비활성화됨)
     * - Chrome 141 버전 불일치로 인해 비활성화
     * - 향후 Selenium 업그레이드 시 재활성화 예정
     */
    private String captureAuthTokenWithCDP(WebDriver driver) {
        log.info("⚠️ CDP capture disabled due to Chrome version mismatch (141 vs v128)");
        return null; // 비활성화
    }
    
    /**
     * JavaScript 후킹 스니펫 주입
     * - fetch/XHR 인터셉트로 실시간 토큰 캡처
     * - localStorage/sessionStorage 스캔
     */
    public void injectTokenHookingScript(WebDriver driver) {
        try {
            log.info("🔧 Injecting JavaScript token hooking script...");
            JavascriptExecutor js = (JavascriptExecutor) driver;
            
            String hookingScript = """
                try {
                  const HEX32 = /^[a-f0-9]{32}$/i;
                  const CSRF = /^[A-Za-z0-9_-]+\\-\\d+$/;

                  function isGood(token) {
                    if (!token || typeof token !== 'string') return false;
                    if (!HEX32.test(token)) return false;
                    if (CSRF.test(token)) return false; // csrf처럼 생긴 값 배제
                    return true;
                  }

                  // 전역 저장소 (window + localStorage)
                  function save(token, source) {
                    try {
                      const payload = {
                        token,
                        source,
                        capturedAt: Date.now()
                      };
                      window.__BUN_TOKEN__ = payload;
                      localStorage.setItem('__BUN_TOKEN__', JSON.stringify(payload));
                      console.log('[TOKEN] captured from', source, token.slice(0,8)+'...'+token.slice(-8));
                    } catch (e) {
                      console.warn('save token error:', e);
                    }
                  }

                  // 1) fetch 후킹
                  if (!window.__FETCH_HOOKED__) {
                    window.__FETCH_HOOKED__ = true;
                    const _fetch = window.fetch;
                    window.fetch = function(input, init = {}) {
                      try {
                        // 요청 URL 파악
                        const url = (typeof input === 'string') ? input : (input && input.url);
                        // 번장 API로 가는 요청만 체크
                        const isBun = url && url.includes('api.bunjang.co.kr');
                        if (isBun && init && init.headers) {
                          // 다양한 형태의 headers 처리
                          const hdrs = (init.headers instanceof Headers)
                            ? Object.fromEntries(init.headers.entries())
                            : (Array.isArray(init.headers) ? Object.fromEntries(init.headers) : {...init.headers});

                          const key = Object.keys(hdrs).find(k => k.toLowerCase() === 'x-bun-auth-token');
                          if (key && isGood(hdrs[key])) save(hdrs[key], 'fetch');
                        }
                      } catch(e) {}
                      return _fetch.apply(this, arguments);
                    };
                  }

                  // 2) XHR 후킹
                  if (!window.__XHR_HOOKED__) {
                    window.__XHR_HOOKED__ = true;
                    const _open = XMLHttpRequest.prototype.open;
                    const _setHeader = XMLHttpRequest.prototype.setRequestHeader;

                    XMLHttpRequest.prototype.open = function(method, url) {
                      this.__reqInfo = { method, url, headers: {} };
                      return _open.apply(this, arguments);
                    };

                    XMLHttpRequest.prototype.setRequestHeader = function(k, v) {
                      try {
                        if (this.__reqInfo) {
                          this.__reqInfo.headers[k] = v;
                        }
                      } catch(e) {}
                      return _setHeader.apply(this, arguments);
                    };

                    const _send = XMLHttpRequest.prototype.send;
                    XMLHttpRequest.prototype.send = function(body) {
                      try {
                        const info = this.__reqInfo || {};
                        const isBun = info.url && info.url.includes('api.bunjang.co.kr');
                        if (isBun && info.headers) {
                          const key = Object.keys(info.headers).find(k => k.toLowerCase() === 'x-bun-auth-token');
                          const val = key ? info.headers[key] : null;
                          if (isGood(val)) save(val, 'xhr');
                        }
                      } catch(e) {}
                      return _send.apply(this, arguments);
                    };
                  }

                  // 3) localStorage / sessionStorage 키 스캔 (보너스)
                  try {
                    const keys = [
                      'x-bun-auth-token','bun-auth-token','auth-token',
                      'bunjang_auth_token','bun_auth_token','authToken'
                    ];
                    for (const k of keys) {
                      const v = localStorage.getItem(k) || sessionStorage.getItem(k);
                      if (isGood(v)) { save(v, 'storage:'+k); break; }
                    }
                  } catch(e) {}

                  console.log('[TOKEN HOOK] Script injected successfully');
                  return true;
                } catch (error) {
                  console.error('[TOKEN HOOK] Script injection failed:', error);
                  return false;
                }
                """;
            
            Object result = js.executeScript(hookingScript);
            if (result instanceof Boolean && (Boolean) result) {
                log.info("✅ JavaScript hooking script injected successfully");
            } else if (result == null) {
                log.warn("⚠️ JavaScript hooking script returned null - script may have failed");
                // null이어도 스크립트는 실행되었을 수 있으므로 계속 진행
            } else {
                log.warn("⚠️ JavaScript hooking script returned unexpected result: {}", result);
            }
            
        } catch (Exception e) {
            log.error("❌ Failed to inject JavaScript hooking script: {}", e.getMessage());
        }
    }
    
    /**
     * 폴링 방식으로 캡처된 토큰 확인 (최대 15초)
     */
    private String pollForCapturedToken(WebDriver driver) {
        try {
            log.info("🔍 Polling for captured token (max 15 seconds)...");
            JavascriptExecutor js = (JavascriptExecutor) driver;
            
            String token = null;
            for (int i = 0; i < 30; i++) { // 30회 * 500ms = 15초
                try {
                    Map<String, Object> payload = (Map<String, Object>) js.executeScript("""
                        try {
                          let p = window.__BUN_TOKEN__ 
                               || JSON.parse(localStorage.getItem('__BUN_TOKEN__')) 
                               || null;
                          return p;
                        } catch(e) { return null; }
                    """);
                    
                    if (payload != null && payload.get("token") != null) {
                        String candidateToken = String.valueOf(payload.get("token"));
                        String source = String.valueOf(payload.get("source"));
                        
                        if (candidateToken.matches("(?i)^[0-9a-f]{32}$") && 
                            !candidateToken.matches("^[A-Za-z0-9_-]+-\\d+$")) {
                            token = candidateToken;
                            log.info("✅ Token captured from {}: {}", source, maskToken(token));
                            break;
                        }
                    }
                    
                    Thread.sleep(500); // 500ms 대기
                    
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.debug("Polling iteration error: {}", e.getMessage());
                }
            }
            
            if (token == null) {
                log.warn("⏳ Token not captured within 15 seconds");
            }
            
            return token;
            
        } catch (Exception e) {
            log.error("❌ Token polling failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 헤더에서 x-bun-auth-token 검색 (대소문자 무관)
     */
    private String findAuthTokenInHeaders(Map<String, Object> headers) {
        return findHeaderIgnoreCase(headers, "x-bun-auth-token");
    }
    
    /**
     * 헤더에서 특정 키 검색 (대소문자 무관)
     */
    private String findHeaderIgnoreCase(Map<String, Object> headers, String headerName) {
        if (headers == null || headerName == null) return null;
        for (Map.Entry<String, Object> en : headers.entrySet()) {
            if (en.getKey() != null && en.getKey().equalsIgnoreCase(headerName)) {
                Object v = en.getValue();
                return (v != null) ? String.valueOf(v) : null;
            }
        }
        return null;
    }

    /**
     * 쿠키 캡처
     * @param driver WebDriver 인스턴스
     * @return 쿠키 리스트
     */
    public List<CookieEntry> captureCookies(WebDriver driver) {
        log.info("🍪 Starting cookie capture process...");
        
        try {
            Set<Cookie> seleniumCookies = driver.manage().getCookies();
            List<CookieEntry> cookieEntries = new ArrayList<>();
            
            for (Cookie cookie : seleniumCookies) {
                CookieEntry entry = new CookieEntry();
                entry.name = cookie.getName();
                entry.value = cookie.getValue();
                entry.domain = cookie.getDomain();
                entry.path = cookie.getPath();
                entry.secure = cookie.isSecure();
                entry.httpOnly = cookie.isHttpOnly();
                
                cookieEntries.add(entry);
                log.debug("🍪 Captured cookie: {}={}", entry.name, entry.value);
            }
            
            log.info("✅ Captured {} cookies", cookieEntries.size());
            return cookieEntries;
            
        } catch (Exception e) {
            log.error("❌ Cookie capture failed: {}", e.getMessage());
            return new ArrayList<>();
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
     * JavaScript로 모든 가능한 위치에서 32자리 hex 토큰 검색 (개선된 버전)
     */
    private String searchAuthTokenWithJavaScript(WebDriver driver) {
        try {
            log.info("🔍 Searching for x-bun-auth-token with comprehensive JavaScript...");
            JavascriptExecutor js = (JavascriptExecutor) driver;
            
            String script = """
                // 모든 가능한 위치에서 32자리 hex 토큰 검색 (개선된 버전)
                function findAuthToken() {
                    const authTokenPattern = /^[a-f0-9]{32}$/i;
                    const csrfPattern = /^[A-Za-z0-9_-]+-\\d+$/;
                    
                    // 1. localStorage에서 검색 (더 정확한 키 검색)
                    const localStorageKeys = [
                        'x-bun-auth-token', 'bun-auth-token', 'auth-token', 
                        'bunjang-auth-token', 'bun_auth_token', 'bunjang_auth_token',
                        'authToken', 'auth_token', 'token', 'access_token'
                    ];
                    
                    for (let key of localStorageKeys) {
                        let value = localStorage.getItem(key);
                        if (value && authTokenPattern.test(value) && !csrfPattern.test(value)) {
                            console.log('Found auth token in localStorage:', key, value);
                            return value;
                        }
                    }
                    
                    // 2. sessionStorage에서 검색
                    for (let key of localStorageKeys) {
                        let value = sessionStorage.getItem(key);
                        if (value && authTokenPattern.test(value) && !csrfPattern.test(value)) {
                            console.log('Found auth token in sessionStorage:', key, value);
                            return value;
                        }
                    }
                    
                    // 3. 모든 localStorage/sessionStorage 키 검색
                    for (let i = 0; i < localStorage.length; i++) {
                        let key = localStorage.key(i);
                        let value = localStorage.getItem(key);
                        if (key && (key.toLowerCase().includes('auth') || key.toLowerCase().includes('token')) && 
                            value && authTokenPattern.test(value) && !csrfPattern.test(value)) {
                            console.log('Found auth token in localStorage (pattern match):', key, value);
                            return value;
                        }
                    }
                    
                    for (let i = 0; i < sessionStorage.length; i++) {
                        let key = sessionStorage.key(i);
                        let value = sessionStorage.getItem(key);
                        if (key && (key.toLowerCase().includes('auth') || key.toLowerCase().includes('token')) && 
                            value && authTokenPattern.test(value) && !csrfPattern.test(value)) {
                            console.log('Found auth token in sessionStorage (pattern match):', key, value);
                            return value;
                        }
                    }
                    
                    // 4. window 객체에서 검색 (더 포괄적)
                    const windowPaths = [
                        'window.bunjang.authToken',
                        'window.bunjang.token',
                        'window.authToken',
                        'window.token',
                        'window.bunjangAuthToken',
                        'window.bunAuthToken'
                    ];
                    
                    for (let path of windowPaths) {
                        try {
                            let value = eval(path);
                            if (value && typeof value === 'string' && authTokenPattern.test(value) && !csrfPattern.test(value)) {
                                console.log('Found auth token in window:', path, value);
                                return value;
                            }
                        } catch (e) {
                            // 접근할 수 없는 속성 무시
                        }
                    }
                    
                    // 5. 네트워크 요청에서 검색 (fetch/xhr 인터셉트)
                    try {
                        // XMLHttpRequest 인터셉트
                        const originalXHROpen = XMLHttpRequest.prototype.open;
                        const originalXHRSend = XMLHttpRequest.prototype.send;
                        
                        XMLHttpRequest.prototype.open = function(method, url) {
                            this._url = url;
                            return originalXHROpen.apply(this, arguments);
                        };
                        
                        XMLHttpRequest.prototype.send = function(data) {
                            if (this._url && this._url.includes('bunjang')) {
                                const headers = this.getAllResponseHeaders();
                                const authTokenMatch = headers.match(/x-bun-auth-token[\\s]*:[\\s]*([a-f0-9]{32})/i);
                                if (authTokenMatch && authTokenMatch[1] && authTokenPattern.test(authTokenMatch[1])) {
                                    console.log('Found auth token in XHR headers:', authTokenMatch[1]);
                                    return authTokenMatch[1];
                                }
                            }
                            return originalXHRSend.apply(this, arguments);
                        };
                    } catch (e) {
                        // 네트워크 인터셉트 실패 무시
                    }
                    
                    // 6. 쿠키에서 검색
                    const cookies = document.cookie.split(';');
                    for (let cookie of cookies) {
                        const [name, value] = cookie.trim().split('=');
                        if (name && (name.toLowerCase().includes('auth') || name.toLowerCase().includes('token')) && 
                            value && authTokenPattern.test(value) && !csrfPattern.test(value)) {
                            console.log('Found auth token in cookies:', name, value);
                            return value;
                        }
                    }
                    
                    return null;
                }
                
                return findAuthToken();
                """;
            
            String token = (String) js.executeScript(script);
            if (token != null && !token.isEmpty() && isValidAuthToken(token)) {
                log.info("✅ Found valid x-bun-auth-token with JavaScript: {}", maskToken(token));
                return token;
            }
            
        } catch (Exception e) {
            log.warn("JavaScript auth token search failed: {}", e.getMessage());
        }
        
        return null;
    }
    
    /**
     * x-bun-auth-token 유효성 검증 (정규식 기반)
     */
    private boolean isValidAuthToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        
        // 32자리 hex 패턴 확인
        if (!AUTH_TOKEN_PATTERN.matcher(token).matches()) {
            return false;
        }
        
        // CSRF 패턴과 일치하면 무효 (authToken == csrf 방지)
        if (CSRF_PATTERN.matcher(token).matches()) {
            log.warn("❌ Invalid authToken: matches CSRF pattern: {}", maskToken(token));
            return false;
        }
        
        return true;
    }

    /**
     * localStorage에서 x-bun-auth-token 검색 (개선된 버전)
     */
    private String searchAuthTokenInLocalStorage(WebDriver driver) {
        try {
            log.info("🔍 Searching for x-bun-auth-token in localStorage...");
            JavascriptExecutor js = (JavascriptExecutor) driver;
            
            // x-bun-auth-token 키로 직접 검색
            String[] authTokenKeys = {
                "x-bun-auth-token",
                "bun-auth-token", 
                "auth-token",
                "bunjang-auth-token",
                "bun_auth_token",
                "bunjang_auth_token"
            };
            
            for (String key : authTokenKeys) {
                try {
                    String value = (String) js.executeScript("return localStorage.getItem('" + key + "');");
                    if (value != null && !value.isEmpty() && isValidAuthToken(value)) {
                        log.info("Found x-bun-auth-token with key '{}' in localStorage", key);
                        return value;
                    }
                } catch (Exception e) {
                    // 키가 존재하지 않는 경우 무시
                }
            }
            
            // 모든 키를 검색하여 32자리 hex 토큰 찾기
            String script = """
                var keys = Object.keys(localStorage);
                for (var i = 0; i < keys.length; i++) {
                    var key = keys[i];
                    var value = localStorage.getItem(key);
                    if (key && key.toLowerCase().includes('auth') && 
                        value && /^[a-f0-9]{32}$/i.test(value)) {
                        console.log('Found auth token in localStorage:', key, value);
                        return value;
                    }
                }
                return null;
                """;
            
            String token = (String) js.executeScript(script);
            if (token != null && !token.isEmpty() && isValidAuthToken(token)) {
                log.info("Found potential x-bun-auth-token in localStorage");
                return token;
            }
            
        } catch (Exception e) {
            log.warn("localStorage x-bun-auth-token search failed: {}", e.getMessage());
        }
        
        return null;
    }
    
    /**
     * sessionStorage에서 x-bun-auth-token 검색
     */
    private String searchAuthTokenInSessionStorage(WebDriver driver) {
        try {
            log.info("🔍 Searching for x-bun-auth-token in sessionStorage...");
            JavascriptExecutor js = (JavascriptExecutor) driver;
            
            // x-bun-auth-token 키로 직접 검색
            String[] authTokenKeys = {
                "x-bun-auth-token",
                "bun-auth-token", 
                "auth-token",
                "bunjang-auth-token",
                "bun_auth_token",
                "bunjang_auth_token"
            };
            
            for (String key : authTokenKeys) {
                try {
                    String value = (String) js.executeScript("return sessionStorage.getItem('" + key + "');");
                    if (value != null && !value.isEmpty() && value.length() >= 20) {
                        log.info("Found x-bun-auth-token with key '{}' in sessionStorage", key);
                        return value;
                    }
                } catch (Exception e) {
                    // 키가 존재하지 않는 경우 무시
                }
            }
            
        } catch (Exception e) {
            log.warn("sessionStorage x-bun-auth-token search failed: {}", e.getMessage());
        }
        
        return null;
    }
    
    /**
     * 쿠키에서 x-bun-auth-token 검색
     */
    private String searchAuthTokenInCookies(WebDriver driver) {
        try {
            log.info("🔍 Searching for x-bun-auth-token in cookies...");
            JavascriptExecutor js = (JavascriptExecutor) driver;
            
            // document.cookie에서 x-bun-auth-token 검색
            String script = """
                var cookies = document.cookie.split(';');
                for (var i = 0; i < cookies.length; i++) {
                    var cookie = cookies[i].trim();
                    var lowerCookie = cookie.toLowerCase();
                    if (lowerCookie.includes('bun') && 
                        lowerCookie.includes('auth') && 
                        lowerCookie.includes('token')) {
                        var value = cookie.split('=')[1];
                        if (value && value.length >= 20) {
                            console.log('Found x-bun-auth-token cookie:', cookie.substring(0, 30) + '...');
                            return value;
                        }
                    }
                }
                return null;
                """;
            
            String token = (String) js.executeScript(script);
            if (token != null && !token.isEmpty()) {
                log.info("Found potential x-bun-auth-token in cookies");
                return token;
            }
            
        } catch (Exception e) {
            log.warn("Cookie x-bun-auth-token search failed: {}", e.getMessage());
        }
        
        return null;
    }
    
    /**
     * 네트워크 요청에서 x-bun-auth-token 검색
     */
    private String searchAuthTokenInNetwork(WebDriver driver) {
        try {
            log.info("🔍 Searching for x-bun-auth-token in network...");
            JavascriptExecutor js = (JavascriptExecutor) driver;
            
            // 페이지의 모든 스크립트에서 x-bun-auth-token 검색
            String script = """
                var scripts = document.getElementsByTagName('script');
                for (var i = 0; i < scripts.length; i++) {
                    var scriptContent = scripts[i].innerHTML;
                    if (scriptContent.includes('x-bun-auth-token') || 
                        scriptContent.includes('bun-auth-token')) {
                        // 토큰 패턴 검색 (32자리 hex 문자열)
                        var match = scriptContent.match(/['"]([a-f0-9]{32})['"]/);
                        if (match) {
                            return match[1];
                        }
                        // 다른 패턴도 시도
                        var match2 = scriptContent.match(/['"]([A-Za-z0-9]{20,})['"]/);
                        if (match2) {
                            return match2[1];
                        }
                    }
                }
                return null;
                """;
            
            String token = (String) js.executeScript(script);
            if (token != null && !token.isEmpty()) {
                log.info("Found potential x-bun-auth-token in network scripts");
                return token;
            }
            
        } catch (Exception e) {
            log.warn("Network x-bun-auth-token search failed: {}", e.getMessage());
        }
        
        return null;
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
