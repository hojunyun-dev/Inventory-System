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
public class Cafe24RegistrationService {
    
    private final WebClient.Builder webClientBuilder;
    private final TokenService tokenService;
    
    @Value("${platforms.cafe24.api-base-url}")
    private String cafe24ApiBaseUrl;
    
    public ProductRegistration registerProduct(ProductRegistrationRequest request) {
        log.info("Registering product on Cafe24: {}", request.getProductName());
        
        try {
            // Get access token
            String accessToken = tokenService.getAccessToken("cafe24");
            
            // Prepare product data for Cafe24 API
            Map<String, Object> productData = prepareCafe24ProductData(request);
            
            // Call Cafe24 API
            Map<String, Object> response = webClientBuilder.build()
                .post()
                .uri(cafe24ApiBaseUrl + "/admin/products")
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .bodyValue(productData)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
            
            // Process response
            return processCafe24Response(request, response);
            
        } catch (Exception e) {
            log.error("Failed to register product on Cafe24: {}", e.getMessage(), e);
            throw new RuntimeException("Cafe24 registration failed: " + e.getMessage());
        }
    }
    
    private Map<String, Object> prepareCafe24ProductData(ProductRegistrationRequest request) {
        Map<String, Object> productData = new HashMap<>();
        
        // Basic product information
        productData.put("product_name", request.getProductName());
        productData.put("product_description", request.getProductDescription());
        productData.put("price", request.getPrice());
        productData.put("stock", request.getStock());
        productData.put("category", request.getCategory());
        
        // Cafe24 specific fields
        productData.put("display", "T"); // Display product
        productData.put("selling", "T"); // Enable selling
        productData.put("product_condition", "U"); // Used condition
        
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
            productData.put("shipping_info", request.getShippingInfo());
        }
        if (request.getReturnPolicy() != null) {
            productData.put("return_policy", request.getReturnPolicy());
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
    
    private ProductRegistration processCafe24Response(ProductRegistrationRequest request, Map<String, Object> response) {
        ProductRegistration registration = new ProductRegistration();
        registration.setPlatform("cafe24");
        registration.setProductId(request.getProductId());
        registration.setProductName(request.getProductName());
        registration.setProductDescription(request.getProductDescription());
        registration.setStatus("SUCCESS");
        registration.setStartedAt(java.time.LocalDateTime.now());
        registration.setCompletedAt(java.time.LocalDateTime.now());
        
        // Extract platform product ID and URL from response
        if (response != null) {
            Object productId = response.get("product_no");
            if (productId != null) {
                registration.setPlatformProductId(productId.toString());
            }
            
            Object productUrl = response.get("product_url");
            if (productUrl != null) {
                registration.setPlatformUrl(productUrl.toString());
            }
        }
        
        return registration;
    }
}

