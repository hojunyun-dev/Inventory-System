package com.inventory.dto;

import lombok.Builder;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Map;

@Data
@Builder
public class RegistrationRequest {
    // 내부 -> registration-service 연동 시 필요한 원본 상품 식별자
    private String productId;
    @NotBlank(message = "Platform is required")
    private String platform;
    
    @NotBlank(message = "Product name is required")
    private String productName;
    
    private String productDescription;
    
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private Double price;
    
    private Integer quantity;
    
    private String category;
    
    private String brand;
    
    private String model;
    
    private String color;
    
    private String size;
    
    private String material;
    
    private String origin;
    
    private String warranty;
    
    private String shippingInfo;
    
    private String returnPolicy;
    
    private String[] images;
    
    private Map<String, String> additionalDetails;
}

