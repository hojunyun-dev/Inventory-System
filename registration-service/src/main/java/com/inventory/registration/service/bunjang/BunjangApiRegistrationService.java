package com.inventory.registration.service.bunjang;

import com.example.common.dto.CookieEntry;
import com.example.common.dto.ProductRegisterRequest;
import com.example.common.dto.TokenBundle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    @Value("${bunjang.api.disable-media:false}")
    private boolean disableMediaUpload;

    @Value("${bunjang.api.image-upload-url:}")
    private String imageUploadUrl;

    @Value("${bunjang.api.image-id-field:imageId}")
    private String imageIdField;

    @Value("${bunjang.seller-id:}")
    private String sellerId;

    @Value("${backend.api.base-url:http://localhost:8080}")
    private String backendBaseUrl;

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
            Map<String, Object> apiResp = callBunjangApi(url, requestBody, headers);

            // 5. 등록 성공 시 백엔드에 콜백(채널별 상품관리 반영)
            try {
                String pid = null;
                if (apiResp != null && apiResp.get("data") instanceof Map<?,?> dataMap) {
                    Object pidObj = ((Map<?,?>) dataMap).get("pid");
                    if (pidObj != null) pid = String.valueOf(pidObj);
                }

                if (pid != null && !pid.isBlank()) {
                    String platformUrl = "https://bunjang.co.kr/products/" + pid;
                    String cbUrl = backendBaseUrl + "/api/channel-products/callback";

                    Map<String, Object> cbBody = new LinkedHashMap<>();
                    try {
                        Long productIdLong = request.productId != null ? Long.valueOf(request.productId) : null;
                        cbBody.put("productId", productIdLong);
                    } catch (Exception e) {
                        log.warn("Invalid productId for callback: {}", request.productId);
                        cbBody.put("productId", null);
                    }
                    cbBody.put("channel", "BUNJANG");
                    cbBody.put("platformProductId", pid);
                    cbBody.put("platformUrl", platformUrl);

                    try {
                        // 서명 생성(옵션): CALLBACK_SECRET 존재 시 적용
                        String secret = System.getenv("CALLBACK_SECRET");
                        org.springframework.web.reactive.function.client.WebClient.RequestBodySpec spec = webClient.post()
                            .uri(cbUrl)
                            .contentType(MediaType.APPLICATION_JSON);

                        if (secret != null && !secret.isBlank()) {
                            String canonical = String.valueOf(cbBody.get("productId")) + "|" + cbBody.get("channel") + "|" + cbBody.get("platformProductId") + "|" + cbBody.get("platformUrl");
                            String sig = hmacSha256Hex(secret, canonical);
                            spec = spec.header("X-Signature", sig);
                        }

                        spec.bodyValue(cbBody)
                            .retrieve()
                            .toBodilessEntity()
                            .timeout(Duration.ofSeconds(5))
                            .onErrorResume(err -> {
                                log.warn("Channel product callback failed: {}", err.getMessage());
                                return Mono.empty();
                            })
                            .block();
                        log.info("🔔 Channel product callback sent: productId={}, pid={}", request.productId, pid);
                    } catch (Exception e) {
                        log.warn("Channel product callback error: {}", e.getMessage());
                    }
                } else {
                    log.info("Callback skipped: pid not found in response (likely pending or failure).");
                }
            } catch (Exception e) {
                log.warn("Callback processing error: {}", e.getMessage());
            }

            return apiResp;
            
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
        if (disableMediaUpload) {
            // 즉시 조치: API 경로의 이미지 업로드/매핑 비활성화 (폼 경로만 사용)
            log.warn("🛑 Media upload disabled by config (bunjang.api.disable-media=true). Skipping imageId mapping.");
            // media 비우거나 정책상 0을 넣지 않음 -> 플랫폼이 기본 이미지 사용하도록 위임
        } else {
            // 실제 이미지 업로드 후 imageId 설정 (구현되어 있을 때만)
            Long imageId = uploadProductImage(request);
            if (imageId != null && imageId > 0) {
                Map<String, Object> mediaItem = new LinkedHashMap<>();
                mediaItem.put("imageId", imageId);
                media.add(mediaItem);
            } else {
                log.error("❌ Image upload did not return imageId. Aborting product registration to avoid 4xx.");
                throw new RuntimeException("Image upload failed: no imageId returned");
            }
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
     * 상품 이미지 업로드 (DB에서 이미지 데이터 조회)
     */
    private Long uploadProductImage(ProductRegisterRequest request) {
        try {
            // 1. 상품 ID 확인
            String productId = request.productId;
            if (productId == null || productId.isEmpty()) {
                log.warn("⚠️ Product ID not provided, skipping image upload");
                return null;
            }
            
            // 2. 백엔드에서 상품 DTO 조회 후 대표 이미지(firstImageUrl) 경로 사용
            String productApi = backendBaseUrl + "/api/products/" + productId;
            Map<String, Object> productDto = webClient.get()
                .uri(productApi)
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<java.util.Map<String, Object>>() {})
                .block();

            if (productDto == null || productDto.get("firstImageUrl") == null) {
                log.warn("⚠️ No representative image (firstImageUrl) found for product: {}", productId);
                return null;
            }

            String firstImageUrl = String.valueOf(productDto.get("firstImageUrl"));
            String imageUrl = firstImageUrl.startsWith("http") ? firstImageUrl : (backendBaseUrl + firstImageUrl);
            log.info("🧭 API image source resolve - productId: {}, firstImageUrl: {}, finalUrl: {}", productId, firstImageUrl, imageUrl);

            ResponseEntity<byte[]> response = webClient.get()
                .uri(imageUrl)
                .retrieve()
                .toEntity(byte[].class)
                .block();
            
            if (response == null || response.getBody() == null || response.getBody().length == 0) {
                log.warn("⚠️ No image data found for product: {}", productId);
                return null;
            }
            
            // 3. 이미지 데이터와 메타데이터 추출
            byte[] imageData = response.getBody();
            String md5 = computeMd5Hex(imageData);
            String contentType = response.getHeaders().getContentType() != null ? 
                response.getHeaders().getContentType().toString() : "image/jpeg";
            
            log.info("✅ Image data retrieved - Product ID: {}, Size: {} bytes, Type: {}, MD5: {}", 
                    productId, imageData.length, contentType, md5);
            
            // 4. 번개장터에 이미지 업로드
            return uploadImageToBunjang(imageData, contentType);
            
        } catch (Exception e) {
            log.error("❌ Image upload failed: {}", e.getMessage());
            return null;
        }
    }

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
     * 번개장터에 이미지 업로드 (바이너리 데이터 사용)
     */
    private Long uploadImageToBunjang(byte[] imageData, String contentType) {
        try {
            log.info("🖼️ Uploading image to Bunjang (multipart) - Size: {} bytes, Type: {}", imageData.length, contentType);

            // 멀티파트 본문 구성 (filename은 고정 규칙 적용)
            org.springframework.util.LinkedMultiValueMap<String, Object> body = new org.springframework.util.LinkedMultiValueMap<>();
            org.springframework.core.io.ByteArrayResource resource = new org.springframework.core.io.ByteArrayResource(imageData) {
                @Override
                public String getFilename() {
                    return "upload_" + System.currentTimeMillis() + ".jpg"; // 서버가 확장자 기반 처리 시 호환성 고려
                }
            };

            org.springframework.http.HttpHeaders partHeaders = new org.springframework.http.HttpHeaders();
            partHeaders.setContentType(MediaType.parseMediaType(contentType != null ? contentType : "image/jpeg"));
            org.springframework.http.HttpEntity<org.springframework.core.io.ByteArrayResource> filePart =
                new org.springframework.http.HttpEntity<>(resource, partHeaders);

            body.add("file", filePart);

            // 토큰/헤더 구성
            TokenBundle tokenBundle = tokenBundleService.getTokenBundle("BUNJANG");
            HttpHeaders headers = buildHeaders(tokenBundle);
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.add(HttpHeaders.ORIGIN, "https://m.bunjang.co.kr");
            headers.add(HttpHeaders.REFERER, "https://m.bunjang.co.kr/");

            // 업로드 호출
            String uploadUrl = resolveUploadUrl();
            Map<String, Object> resp = webClient.post()
                .uri(uploadUrl)
                .headers(h -> h.addAll(headers))
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(20))
                .block();

            if (resp == null || resp.isEmpty()) {
                log.warn("⚠️ Empty response from image upload API");
                return null;
            }

            // 전체 응답 로그 (요약)
            log.info("🧾 Image upload API raw response (summary): {}", summarize(String.valueOf(resp)));

            // imageId 필드 파싱
            Object idObj = resp.get(imageIdField);
            if (idObj == null) {
                // 일반적인 대체 키 후보도 탐색
                idObj = resp.get("id");
                if (idObj == null && resp.containsKey("data") && resp.get("data") instanceof Map<?,?> data) {
                    idObj = ((Map<?,?>) data).get(imageIdField);
                    if (idObj == null) idObj = ((Map<?,?>) data).get("id");
                }
                // files[0].id 형태 탐색
                if (idObj == null && resp.containsKey("files") && resp.get("files") instanceof java.util.List<?> files && !files.isEmpty()) {
                    Object first = files.get(0);
                    if (first instanceof Map<?,?> fm) {
                        idObj = fm.get("id");
                        if (idObj == null) idObj = fm.get(imageIdField);
                        if (idObj == null) idObj = fm.get("seq");
                    }
                }
                // image_id 키 우선 탐색(벤더 응답 케이스)
                if (idObj == null) {
                    idObj = resp.get("image_id");
                }
            }

            Long parsedId = null;
            if (idObj instanceof Number num) {
                parsedId = num.longValue();
            } else if (idObj != null) {
                try { parsedId = Long.parseLong(String.valueOf(idObj)); } catch (Exception ignore) {}
            }

            log.info("✅ Image upload API response parsed - raw: {}, imageId: {}", summarize(String.valueOf(resp)), parsedId);
            return parsedId;

        } catch (Exception e) {
            log.error("❌ Bunjang image upload failed: {}", e.getMessage(), e);
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
                .onStatus(HttpStatusCode::is4xxClientError, resp -> {
                    // 401/403 오류 시 토큰 삭제
                    int statusCode = resp.statusCode().value();
                    if (statusCode == 401 || statusCode == 403) {
                        log.warn("🚨 인증 오류 감지 ({}). DB 토큰을 삭제합니다.", statusCode);
                        try {
                            tokenBundleService.deleteTokenBundle("BUNJANG");
                            log.info("✅ 토큰 삭제 완료");
                        } catch (Exception e) {
                            log.warn("토큰 삭제 중 오류: {}", e.getMessage());
                        }
                    }
                    
                    return resp.bodyToMono(String.class).defaultIfEmpty("")
                        .map(body -> {
                            log.warn("❌ 4xx from Bunjang ({}): {}", statusCode, maskBody(body));
                            return new RuntimeException("Bunjang 4xx: " + summarize(body));
                        });
                })
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

    private String hmacSha256Hex(String secret, String data) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            mac.init(new javax.crypto.spec.SecretKeySpec(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] raw = mac.doFinal(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : raw) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            log.warn("HMAC generation failed: {}", e.getMessage());
            return "";
        }
    }

    /**
     * 토큰 유효성 검사
     */
    public boolean isTokenValid() {
        TokenBundle tokenBundle = tokenBundleService.getTokenBundle("BUNJANG");
        return tokenBundle != null && !tokenBundleService.isExpired(tokenBundle);
    }

    private String resolveUploadUrl() {
        if (imageUploadUrl != null && !imageUploadUrl.isBlank()) return imageUploadUrl;
        if (sellerId != null && !sellerId.isBlank()) {
            return "https://media-center.bunjang.co.kr/upload/" + sellerId + "/product";
        }
        // 기본값 미지정 시 예외 처리
        throw new IllegalStateException("Image upload URL not configured. Set bunjang.api.image-upload-url or bunjang.seller-id.");
        
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


