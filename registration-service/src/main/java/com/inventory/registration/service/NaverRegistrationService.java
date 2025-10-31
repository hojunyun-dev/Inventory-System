package com.inventory.registration.service;

import com.inventory.registration.dto.ProductRegistrationRequest;
import com.inventory.registration.entity.ProductRegistration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NaverRegistrationService {
    
    private final WebClient.Builder webClientBuilder;
    
    @Value("${platforms.naver.api-base-url}")
    private String naverApiBaseUrl;
    
    public ProductRegistration registerProduct(ProductRegistrationRequest request) {
        log.info("Registering product on Naver: {}", request.getProductName());
        
        try {
            // Get access token - placeholder implementation
            String accessToken = "placeholder_token";
            
            // Prepare product data for Naver API
            Map<String, Object> productData = prepareNaverProductData(request);
            
            // Call Naver API
            Map<String, Object> response = webClientBuilder.build()
                .post()
                .uri(naverApiBaseUrl + "/products")
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .bodyValue(productData)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
            
            // Process response
            return processNaverResponse(request, response);
            
        } catch (Exception e) {
            log.error("Failed to register product on Naver: {}", e.getMessage(), e);
            throw new RuntimeException("Naver registration failed: " + e.getMessage());
        }
    }
    
    private Map<String, Object> prepareNaverProductData(ProductRegistrationRequest request) {
        Map<String, Object> productData = new HashMap<>();
        
        // Basic product information
        productData.put("name", request.getProductName());
        productData.put("description", request.getProductDescription());
        productData.put("price", request.getPrice());
        productData.put("stock", request.getStock());
        productData.put("category", request.getCategory());
        
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
    
    private ProductRegistration processNaverResponse(ProductRegistrationRequest request, Map<String, Object> response) {
        ProductRegistration registration = new ProductRegistration();
        registration.setPlatform("naver");
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

