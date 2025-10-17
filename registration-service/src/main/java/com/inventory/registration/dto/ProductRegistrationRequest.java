package com.inventory.registration.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

@Data
public class ProductRegistrationRequest {
    @NotBlank(message = "Platform is required")
    private String platform;

    @NotBlank(message = "Product ID is required")
    private String productId;

    @NotBlank(message = "Product name is required")
    private String productName;

    private String productDescription;
    private String category;
    private Double price;
    private Integer stock;
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
    private Map<String, Object> customAttributes;
    private String templateId; // 사용할 템플릿 ID
    private Boolean useAutomation = false; // 자동화 사용 여부
}

