package com.inventory.registration.service;

import com.inventory.registration.dto.ProductRegistrationRequest;
import com.inventory.registration.entity.ProductRegistration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoupangRegistrationService {
    
    private final WebClient.Builder webClientBuilder;
    private final TokenService tokenService;
    
    @Value("${platforms.coupang.api-base-url}")
    private String coupangApiBaseUrl;
    
    public ProductRegistration registerProduct(ProductRegistrationRequest request) {
        log.info("Registering product on Coupang: {}", request.getProductName());
        
        try {
            // Get access token
            String accessToken = tokenService.getAccessToken("coupang");
            
            // Prepare product data for Coupang API
            Map<String, Object> productData = prepareCoupangProductData(request);
            
            // Call Coupang API
            Map<String, Object> response = webClientBuilder.build()
                .post()
                .uri(coupangApiBaseUrl + "/v2/providers/affiliate_open_api/apis/openapi/products")
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .bodyValue(productData)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
            
            // Process response
            return processCoupangResponse(request, response);
            
        } catch (Exception e) {
            log.error("Failed to register product on Coupang: {}", e.getMessage(), e);
            throw new RuntimeException("Coupang registration failed: " + e.getMessage());
        }
    }
    
    private Map<String, Object> prepareCoupangProductData(ProductRegistrationRequest request) {
        Map<String, Object> productData = new HashMap<>();
        
        // Basic product information
        productData.put("productName", request.getProductName());
        productData.put("productDescription", request.getProductDescription());
        productData.put("price", request.getPrice());
        productData.put("stock", request.getStock());
        productData.put("category", request.getCategory());
        
        // Coupang specific fields
        productData.put("vendorId", "your_vendor_id"); // 실제 벤더 ID로 교체 필요
        productData.put("productType", "NORMAL"); // 일반 상품
        productData.put("saleType", "SALE"); // 판매 가능
        
        // Additional attributes
        if (request.getBrand() != null) {
            productData.put("brand", request.getBrand());
        }
        if (request.getModel() != null) {
            productData.put("model", request.getModel());
        }
        if (request.getColor() != null) {
            productData.put("color", request.getColor());
        }
        if (request.getSize() != null) {
            productData.put("size", request.getSize());
        }
        if (request.getMaterial() != null) {
            productData.put("material", request.getMaterial());
        }
        if (request.getOrigin() != null) {
            productData.put("origin", request.getOrigin());
        }
        if (request.getWarranty() != null) {
            productData.put("warranty", request.getWarranty());
        }
        if (request.getShippingInfo() != null) {
            productData.put("shippingInfo", request.getShippingInfo());
        }
        if (request.getReturnPolicy() != null) {
            productData.put("returnPolicy", request.getReturnPolicy());
        }
        if (request.getImages() != null) {
            productData.put("images", request.getImages());
        }
        
        // Custom attributes
        if (request.getCustomAttributes() != null) {
            productData.putAll(request.getCustomAttributes());
        }
        
        return productData;
    }
    
    private ProductRegistration processCoupangResponse(ProductRegistrationRequest request, Map<String, Object> response) {
        ProductRegistration registration = new ProductRegistration();
        registration.setPlatform("coupang");
        registration.setProductId(request.getProductId());
        registration.setProductName(request.getProductName());
        registration.setProductDescription(request.getProductDescription());
        registration.setStatus("SUCCESS");
        registration.setStartedAt(java.time.LocalDateTime.now());
        registration.setCompletedAt(java.time.LocalDateTime.now());
        
        // Extract platform product ID and URL from response
        if (response != null) {
            Object productId = response.get("productId");
            if (productId != null) {
                registration.setPlatformProductId(productId.toString());
            }
            
            Object productUrl = response.get("productUrl");
            if (productUrl != null) {
                registration.setPlatformUrl(productUrl.toString());
            }
        }
        
        return registration;
    }
}

