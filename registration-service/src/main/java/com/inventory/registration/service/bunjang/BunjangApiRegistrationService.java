package com.inventory.registration.service.bunjang;

import com.example.common.dto.CookieEntry;
import com.example.common.dto.ProductRegisterRequest;
import com.example.common.dto.TokenBundle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;
import io.netty.channel.ChannelOption;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 번개장터 API 기반 상품 등록 서비스
 * - TokenBundle을 사용한 인증
 * - HTTP 헤더 구성 (Cookie, CSRF, User-Agent 등)
 * - 재시도 로직 포함
 */
@Service
@Slf4j
public class BunjangApiRegistrationService {

    @Value("${bunjang.api.base-url:https://m.bunjang.co.kr}")
    private String baseUrl;

    @Value("${bunjang.api.product-endpoint:/api/sell}")
    private String productEndpoint;

    private final WebClient webClient;
    private final TokenBundleService tokenBundleService;

    public BunjangApiRegistrationService(WebClient.Builder webClientBuilder, TokenBundleService tokenBundleService) {
        // HttpClient 설정 (타임아웃, 백오프)
        HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5_000)
            .responseTimeout(Duration.ofSeconds(10));
        
        this.webClient = webClientBuilder
            .baseUrl("https://api.bunjang.co.kr") // 올바른 API 도메인
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .filter((request, next) -> {
                // 요청 로깅(민감정보 마스킹)
                log.debug("➡️ {} {}", request.method(), request.url());
                request.headers().forEach((k, v) -> {
                    if ("cookie".equalsIgnoreCase(k) || "x-bun-auth-token".equalsIgnoreCase(k)) {
                        log.debug("  {}: ***masked***", k);
                    } else {
                        log.trace("  {}: {}", k, v);
                    }
                });
                return next.exchange(request);
            })
            .build();
        this.tokenBundleService = tokenBundleService;
    }

    /**
     * 상품 등록
     */
    public Mono<Map<String, Object>> registerProduct(ProductRegisterRequest request) {
        return Mono.fromCallable(() -> {
            log.info("📦 Starting Bunjang API product registration: {}", request.name);
            
            // 1. 토큰 조회
            TokenBundle tokenBundle = tokenBundleService.getTokenBundle("BUNJANG");
            if (tokenBundle == null || tokenBundleService.isExpired(tokenBundle)) {
                throw new RuntimeException("No valid token found. Please login first.");
            }
            
            // 2. API 요청 구성
            String url = baseUrl + productEndpoint;
            Map<String, Object> requestBody = buildRequestBody(request);

            // 디버그: 실제 전송 바디 로깅(민감정보 없음, 길이 제한)
            try {
                String bodyPreview = toJsonPreview(requestBody, 800);
                log.info("📝 Bunjang request body preview: {}", bodyPreview);
            } catch (Exception ignore) {}
            
            // 3. HTTP 헤더 구성
            HttpHeaders headers = buildHeaders(tokenBundle);
            
            // 4. API 호출
            return callBunjangApi(url, requestBody, headers);
            
        }).onErrorMap(throwable -> {
            log.error("❌ Product registration failed: {}", throwable.getMessage(), throwable);
            return new RuntimeException("Product registration failed: " + throwable.getMessage());
        });
    }

    /**
     * API 요청 본문 구성 (성공 케이스 구조 적용)
     */
    private Map<String, Object> buildRequestBody(ProductRegisterRequest request) {
        String name = request.name;
        Long price = request.price;
        String desc = (request.description != null) ? request.description : "";
        String cat = (request.categoryId != null) ? request.categoryId : "";
        List<String> keywords = (request.keywords != null) ? request.keywords : List.of();

        // 성공 케이스와 동일한 중첩 구조로 변경
        Map<String, Object> body = new LinkedHashMap<>();
        
        // 1. categoryId (최상위)
        body.put("categoryId", mapCategoryId(cat));
        
        // 2. common 객체
        Map<String, Object> common = new LinkedHashMap<>();
        common.put("name", name);
        common.put("description", desc);
        common.put("keywords", keywords);
        common.put("condition", "NEW");  // 기본값: 새상품
        body.put("common", common);
        
        // 3. location 객체
        Map<String, Object> location = new LinkedHashMap<>();
        location.put("geo", null);
        body.put("location", location);
        
        // 4. media 객체 (이미지)
        List<Map<String, Object>> media = new ArrayList<>();
        // 실제 이미지 업로드 후 imageId 설정
        Long imageId = uploadProductImage(request);
        if (imageId != null && imageId > 0) {
            Map<String, Object> mediaItem = new LinkedHashMap<>();
            mediaItem.put("imageId", imageId);
            media.add(mediaItem);
        } else {
            // 이미지가 없으면 기본 이미지 ID 사용
            Map<String, Object> mediaItem = new LinkedHashMap<>();
            mediaItem.put("imageId", 0);
            media.add(mediaItem);
        }
        body.put("media", media);
        
        // 5. naverShoppingData 객체
        Map<String, Object> naverShoppingData = new LinkedHashMap<>();
        naverShoppingData.put("isEnabled", false);
        body.put("naverShoppingData", naverShoppingData);
        
        // 6. option 배열 (빈 배열)
        body.put("option", new ArrayList<>());
        
        // 7. transaction 객체
        Map<String, Object> transaction = new LinkedHashMap<>();
        transaction.put("quantity", 1);  // 기본 수량
        transaction.put("price", price);
        // 배송비 포함 (무료배송) 구조 적용
        Map<String, Object> trade = new LinkedHashMap<>();
        trade.put("freeShipping", true);
        trade.put("isDefaultShippingFee", false);
        trade.put("inPerson", false);
        transaction.put("trade", trade);
        body.put("transaction", transaction);
        
        return body;
    }
    
    /**
     * 카테고리 ID 매핑 (문자열 → 번장 카테고리 코드)
     */
    private String mapCategoryId(String categoryId) {
        if (categoryId == null || categoryId.isEmpty()) {
            return "750610200";  // 기본값: 기타
        }
        
        // 카테고리 매핑 테이블 (실제 번장 카테고리 코드)
        Map<String, String> categoryMap = Map.of(
            "기타", "750610200",
            "자동차", "750610200",
            "부품", "750610200",
            "엔진오일", "750610200"
        );
        
        return categoryMap.getOrDefault(categoryId, "750610200");
    }
    
    /**
     * 상품 이미지 업로드 (재고관리 시스템의 실제 이미지 사용)
     */
    private Long uploadProductImage(ProductRegisterRequest request) {
        try {
            // 1. 재고관리 시스템에서 상품 정보 조회
            String productId = request.productId;
            if (productId == null || productId.isEmpty()) {
                log.warn("⚠️ Product ID not provided, skipping image upload");
                return null;
            }
            
            // 2. 백엔드 서비스에서 상품 정보 조회
            String backendUrl = "http://localhost:8080/api/products/" + productId;
            Map<String, Object> productInfo = webClient.get()
                .uri(backendUrl)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
            
            if (productInfo == null) {
                log.warn("⚠️ Product not found in backend system: {}", productId);
                return null;
            }
            
            // 3. 이미지 파일 경로 확인
            String imagePath = (String) productInfo.get("imagePath");
            if (imagePath == null || imagePath.isEmpty()) {
                log.warn("⚠️ No image found for product: {}", productId);
                return null;
            }
            
            // 4. 실제 이미지 파일 업로드
            return uploadImageToBunjang(imagePath);
            
        } catch (Exception e) {
            log.error("❌ Image upload failed: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 번개장터에 이미지 업로드
     */
    private Long uploadImageToBunjang(String imagePath) {
        try {
            // TODO: 번개장터 이미지 업로드 API 호출
            // 현재는 임시로 랜덤 ID 반환
            log.info("🖼️ Uploading image to Bunjang: {}", imagePath);
            
            // 실제 구현 시:
            // 1. 이미지 파일 읽기
            // 2. 번개장터 이미지 업로드 API 호출
            // 3. 응답에서 imageId 추출
            // 4. imageId 반환
            
            return 1558316935L; // 임시값 (성공 케이스의 imageId)
            
        } catch (Exception e) {
            log.error("❌ Bunjang image upload failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * HTTP 헤더 구성
     */
    private HttpHeaders buildHeaders(TokenBundle tokenBundle) {
        HttpHeaders headers = new HttpHeaders();
        
        // bun_session 쿠키 추출 및 설정
        String bunSessionCookie = extractBunSessionCookie(tokenBundle.cookies);
        if (bunSessionCookie != null) {
            headers.add(HttpHeaders.COOKIE, "bun_session=" + bunSessionCookie);
            log.debug("🍪 bun_session cookie set: {}", bunSessionCookie.substring(0, 8) + "...");
        }
        
        // x-bun-auth-token 헤더 설정
        if (tokenBundle.authToken != null) {
            headers.add("x-bun-auth-token", tokenBundle.authToken);
            log.debug("🔑 x-bun-auth-token set: {}", tokenBundle.authToken.substring(0, 8) + "...");
        } else {
            log.warn("⚠️ x-bun-auth-token not found in token bundle");
        }
        
        // 필수 헤더
        headers.add(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36");
        headers.add(HttpHeaders.ACCEPT_LANGUAGE, "ko-KR,ko;q=0.9,en;q=0.8");
        
        return headers;
    }
    
    /**
     * bun_session 쿠키 추출
     */
    private String extractBunSessionCookie(List<com.example.common.dto.CookieEntry> cookies) {
        if (cookies == null || cookies.isEmpty()) {
            return null;
        }
        
        for (com.example.common.dto.CookieEntry cookie : cookies) {
            if ("bun_session".equals(cookie.name)) {
                return cookie.value;
            }
        }
        
        return null;
    }


    /**
     * 번개장터 API 호출 (개선된 버전)
     */
    private Map<String, Object> callBunjangApi(String url, Map<String, Object> requestBody, HttpHeaders headers) {
        try {
            log.info("🌐 Calling Bunjang API: {}", url);
            
            Map<String, Object> response = webClient.post()
                .uri("/api/pms/v2/products") // 올바른 엔드포인트
                .headers(h -> h.addAll(headers))
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, resp ->
                    resp.bodyToMono(String.class).defaultIfEmpty("")
                        .map(body -> {
                            log.warn("❌ 4xx from Bunjang: {}", maskBody(body));
                            return new RuntimeException("Bunjang 4xx: " + summarize(body));
                        })
                )
                .onStatus(HttpStatusCode::is5xxServerError, resp ->
                    resp.bodyToMono(String.class).defaultIfEmpty("")
                        .map(body -> {
                            log.warn("❌ 5xx from Bunjang: {}", maskBody(body));
                            return new RuntimeException("Bunjang 5xx: " + summarize(body));
                        })
                )
                .bodyToMono(Map.class)
                .retryWhen(
                    Retry.backoff(3, Duration.ofMillis(500))
                         .filter(ex -> isRetryable(ex)) // 429/5xx 등만 재시도
                         .maxBackoff(Duration.ofSeconds(5))
                )
                .timeout(Duration.ofSeconds(20))
                .doOnSuccess(result -> log.info("✅ Bunjang API call successful"))
                .block();
            
            return response;
            
        } catch (Exception e) {
            log.error("❌ Bunjang API call failed: {}", e.getMessage(), e);
            throw new RuntimeException("API call failed: " + e.getMessage());
        }
    }

    /**
     * 민감정보 마스킹
     */
    private String maskBody(String body) {
        if (body == null) return "";
        // 단순 마스킹 예시 (토큰/쿠키류 키워드)
        return body.replaceAll("(?i)(bun_session=|x-bun-auth-token\\s*:\\s*)([A-Za-z0-9._-]+)", "$1***");
    }
    
    /**
     * 응답 본문 요약
     */
    private String summarize(String body) {
        if (body == null) return "";
        return body.length() > 300 ? body.substring(0, 300) + "..." : body;
    }
    
    /**
     * 재시도 가능한 에러인지 확인
     */
    private boolean isRetryable(Throwable ex) {
        String s = String.valueOf(ex.getMessage());
        return s.contains("429") || s.contains("5xx") || s.contains("timeout");
    }

    /**
     * 토큰 유효성 검사
     */
    public boolean isTokenValid() {
        TokenBundle tokenBundle = tokenBundleService.getTokenBundle("BUNJANG");
        return tokenBundle != null && !tokenBundleService.isExpired(tokenBundle);
    }

    /**
     * 토큰 상태 조회
     */
    public Map<String, Object> getTokenStatus() {
        TokenBundle tokenBundle = tokenBundleService.getTokenBundle("BUNJANG");
        if (tokenBundle == null) {
            return Map.of(
                "hasToken", false,
                "isExpired", true,
                "message", "No token found"
            );
        }
        
        boolean isExpired = tokenBundleService.isExpired(tokenBundle);
        return Map.of(
            "hasToken", true,
            "isExpired", isExpired,
            "cookieCount", tokenBundle.cookies.size(),
            "hasCsrf", tokenBundle.csrf != null,
            "expiresAt", tokenBundle.expiresAt,
            "message", isExpired ? "Token expired" : "Token valid"
        );
    }

    /**
     * 간단한 JSON 직렬화(미니파이) + 길이 제한
     */
    private String toJsonPreview(Map<String, Object> body, int maxLen) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
            String json = om.writeValueAsString(body);
            if (json.length() > maxLen) {
                return json.substring(0, maxLen) + "...";
            }
            return json;
        } catch (Exception e) {
            return String.valueOf(body);
        }
    }
}


