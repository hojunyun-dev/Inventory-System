package com.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private Long id;
    private String sku;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal cost;
    private String barcode;
    private Integer quantity;
    private Integer minimumQuantity;
    private Boolean isActive;
    private Boolean isSerialized;
    private Long categoryId;
    private String categoryName;
    private String imageUrl; // 레거시 이미지 URL
    private Integer imageCount; // 이미지 개수
    private String firstImageUrl; // 첫 번째 이미지 URL (API 엔드포인트)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

