package com.inventory.service;

import com.inventory.dto.PlatformProductRequest;
import com.inventory.dto.PlatformProductResponse;
import com.inventory.dto.TokenResponse;
import com.inventory.dto.RegistrationRequest;
import com.inventory.dto.RegistrationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@Slf4j
public class PlatformIntegrationService {

    private final WebClient tokenServiceWebClient;
    private final TokenManagementService tokenManagementService;
    private final RegistrationService registrationService;
    
    public PlatformIntegrationService(
            @Qualifier("tokenServiceWebClient") WebClient tokenServiceWebClient,
            TokenManagementService tokenManagementService,
            RegistrationService registrationService) {
        this.tokenServiceWebClient = tokenServiceWebClient;
        this.tokenManagementService = tokenManagementService;
        this.registrationService = registrationService;
    }

    @Value("${token-management-service.url:http://localhost:8081}")
    private String tokenManagementServiceUrl;

    @Value("${registration-service.url:http://localhost:8082}")
    private String registrationServiceUrl;

    /**
     * 플랫폼별 상품 등록
     */
    public Mono<PlatformProductResponse> registerProductToPlatform(String platform, PlatformProductRequest request) {
        log.info("Registering product to platform: {}, product: {}", platform, request.getProductName());
        
        return switch (platform.toLowerCase()) {
            case "naver" -> registerToNaver(request);
            case "cafe24" -> registerToCafe24(request);
            case "coupang" -> registerToCoupang(request);
            case "bunjang" -> registerToBunjang(request);
            case "danggeun" -> registerToDanggeun(request);
            default -> Mono.error(new IllegalArgumentException("Unsupported platform: " + platform));
        };
    }

    /**
     * 네이버 쇼핑 등록
     */
    private Mono<PlatformProductResponse> registerToNaver(PlatformProductRequest request) {
        return tokenManagementService.getToken("naver")
                .flatMap(token -> {
                    // 토큰을 사용하여 네이버 API 호출
                    return tokenServiceWebClient.post()
                            .uri("https://api.commerce.naver.com/products")
                            .header("Authorization", "Bearer " + token.getAccessToken())
                            .bodyValue(buildNaverPayload(request))
                            .retrieve()
                            .bodyToMono(Map.class)
                            .map(response -> {
                                Map<String, Object> responseMap = (Map<String, Object>) response;
                                return PlatformProductResponse.builder()
                                        .platform("naver")
                                        .externalProductId(responseMap.get("productId").toString())
                                        .status("SUCCESS")
                                        .message("Product registered successfully to Naver")
                                        .build();
                            });
                })
                .onErrorResume(e -> {
                    log.error("Failed to register to Naver: {}", e.getMessage());
                    return Mono.just(PlatformProductResponse.builder()
                            .platform("naver")
                            .status("FAILED")
                            .message("Failed to register to Naver: " + e.getMessage())
                            .build());
                });
    }

    /**
     * 카페24 등록
     */
    private Mono<PlatformProductResponse> registerToCafe24(PlatformProductRequest request) {
        return tokenManagementService.getToken("cafe24")
                .flatMap(token -> {
                    return tokenServiceWebClient.post()
                            .uri("https://your-mall.cafe24api.com/api/v2/admin/products")
                            .header("Authorization", "Bearer " + token.getAccessToken())
                            .bodyValue(buildCafe24Payload(request))
                            .retrieve()
                            .bodyToMono(Map.class)
                            .map(response -> {
                                Map<String, Object> responseMap = (Map<String, Object>) response;
                                return PlatformProductResponse.builder()
                                        .platform("cafe24")
                                        .externalProductId(responseMap.get("product_no").toString())
                                        .status("SUCCESS")
                                        .message("Product registered successfully to Cafe24")
                                        .build();
                            });
                })
                .onErrorResume(e -> {
                    log.error("Failed to register to Cafe24: {}", e.getMessage());
                    return Mono.just(PlatformProductResponse.builder()
                            .platform("cafe24")
                            .status("FAILED")
                            .message("Failed to register to Cafe24: " + e.getMessage())
                            .build());
                });
    }

    /**
     * 쿠팡 등록
     */
    private Mono<PlatformProductResponse> registerToCoupang(PlatformProductRequest request) {
        return tokenManagementService.getToken("coupang")
                .flatMap(token -> {
                    return tokenServiceWebClient.post()
                            .uri("https://api-gateway.coupang.com/v2/providers/seller_api/apis/api/v1/products")
                            .header("Authorization", "Bearer " + token.getAccessToken())
                            .bodyValue(buildCoupangPayload(request))
                            .retrieve()
                            .bodyToMono(Map.class)
                            .map(response -> {
                                Map<String, Object> responseMap = (Map<String, Object>) response;
                                Map<String, Object> dataMap = (Map<String, Object>) responseMap.get("data");
                                return PlatformProductResponse.builder()
                                        .platform("coupang")
                                        .externalProductId(dataMap.get("productId").toString())
                                        .status("SUCCESS")
                                        .message("Product registered successfully to Coupang")
                                        .build();
                            });
                })
                .onErrorResume(e -> {
                    log.error("Failed to register to Coupang: {}", e.getMessage());
                    return Mono.just(PlatformProductResponse.builder()
                            .platform("coupang")
                            .status("FAILED")
                            .message("Failed to register to Coupang: " + e.getMessage())
                            .build());
                });
    }

    /**
     * 번개장터 등록 (자동화)
     */
    private Mono<PlatformProductResponse> registerToBunjang(PlatformProductRequest request) {
        return registrationService.registerProduct(convertToRegistrationRequest(request, "bunjang"))
                .map(response -> PlatformProductResponse.builder()
                        .platform("bunjang")
                        .externalProductId(response.getExternalProductId())
                        .status(response.getStatus())
                        .message("Product registration " + response.getStatus().toLowerCase())
                        .build());
    }

    /**
     * 당근마켓 등록 (자동화)
     */
    private Mono<PlatformProductResponse> registerToDanggeun(PlatformProductRequest request) {
        return registrationService.registerProduct(convertToRegistrationRequest(request, "danggeun"))
                .map(response -> PlatformProductResponse.builder()
                        .platform("danggeun")
                        .externalProductId(response.getExternalProductId())
                        .status(response.getStatus())
                        .message("Product registration " + response.getStatus().toLowerCase())
                        .build());
    }

    /**
     * 토큰 관리 서비스 연동
     */
    private Mono<TokenResponse> getTokenFromTokenService(String platform) {
        return tokenServiceWebClient.get()
                .uri(tokenManagementServiceUrl + "/api/tokens/" + platform)
                .retrieve()
                .bodyToMono(TokenResponse.class);
    }

    /**
     * 등록 서비스 연동
     */
    private Mono<RegistrationResponse> registerProductViaRegistrationService(RegistrationRequest request) {
        return tokenServiceWebClient.post()
                .uri(registrationServiceUrl + "/api/registrations")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(RegistrationResponse.class);
    }

    // Helper methods for building platform-specific payloads
    private Map<String, Object> buildNaverPayload(PlatformProductRequest request) {
        return Map.of(
            "productName", request.getProductName(),
            "price", request.getPrice(),
            "description", request.getDescription(),
            "category", request.getCategory(),
            "brand", request.getBrand()
        );
    }

    private Map<String, Object> buildCafe24Payload(PlatformProductRequest request) {
        return Map.of(
            "product_name", request.getProductName(),
            "price", request.getPrice(),
            "product_description", request.getDescription(),
            "category", request.getCategory()
        );
    }

    private Map<String, Object> buildCoupangPayload(PlatformProductRequest request) {
        return Map.of(
            "itemName", request.getProductName(),
            "originalPrice", request.getPrice(),
            "vendorItemName", request.getProductName(),
            "displayCategoryCode", request.getCategory()
        );
    }

    private RegistrationRequest convertToRegistrationRequest(PlatformProductRequest request, String platform) {
        return RegistrationRequest.builder()
                .platform(platform)
                .productId(request.getProductId())
                .productName(request.getProductName())
                .productDescription(request.getDescription())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .category(request.getCategory())
                .brand(request.getBrand())
                .build();
    }
}
