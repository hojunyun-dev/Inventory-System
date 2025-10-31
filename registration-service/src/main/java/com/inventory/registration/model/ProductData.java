package com.inventory.registration.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductData {
    private String name;
    private String description;
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
    private List<String> images;
    private Map<String, Object> customAttributes;
    @Builder.Default
    private Boolean priceNegotiation = false;
    @Builder.Default
    private Boolean directTrade = false;
    private String condition; // NEW, USED, REFURBISHED
    private String location;
    private String tags;
    private String platform; // 플랫폼 정보 추가
    private String username; // 사용자명 (자동화용)
    private String password; // 비밀번호 (자동화용)
    
    // 누락된 메서드들 추가
    public List<String> getImagePaths() {
        return this.images;
    }
    
    public void setImagePaths(List<String> imagePaths) {
        this.images = imagePaths;
    }
    
    public String getPlatform() {
        return this.platform;
    }
    
    public void setPlatform(String platform) {
        this.platform = platform;
    }
}