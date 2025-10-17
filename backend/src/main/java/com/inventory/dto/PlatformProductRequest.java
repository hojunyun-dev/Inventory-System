package com.inventory.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
public class PlatformProductRequest {
    @NotBlank(message = "Product name is required")
    private String productName;
    
    private String description;
    
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
}

