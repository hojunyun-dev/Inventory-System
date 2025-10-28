package com.example.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class ProductRegisterRequest {
    @NotBlank public String platform; // "BUNJANG"
    @NotBlank public String name;
    @NotNull public Long price;
    public String description;
    public String categoryId;
    public List<String> keywords;
    
    public ProductRegisterRequest() {}
    
    public ProductRegisterRequest(String platform, String name, Long price, String description, String categoryId, List<String> keywords) {
        this.platform = platform;
        this.name = name;
        this.price = price;
        this.description = description;
        this.categoryId = categoryId;
        this.keywords = keywords;
    }
}


