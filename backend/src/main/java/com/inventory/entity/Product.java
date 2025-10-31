package com.inventory.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String sku;
    
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal cost;
    
    @Column(unique = true)
    private String barcode;
    
    @Column(nullable = false)
    private Integer quantity = 0;
    
    @Column(nullable = false)
    private Integer minimumQuantity = 0;
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
    @Column(nullable = false)
    private Boolean isSerialized = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonIgnore
    private Category category;
    
    // 카탈로그 표준화 필드 추가
    @Enumerated(EnumType.STRING)
    @Column(name = "part_type")
    private PartType partType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "part_condition")
    private PartCondition partCondition;
    
    @Column(name = "oem_part_number")
    private String oemPartNumber;
    
    @Column(name = "aftermarket_part_number")
    private String aftermarketPartNumber;
    
    @Column(name = "manufacturer_code")
    private String manufacturerCode;
    
    @Column(name = "manufacturer_name")
    private String manufacturerName;
    
    @Column(name = "cross_reference_numbers", columnDefinition = "TEXT")
    private String crossReferenceNumbers; // JSON 형태로 저장
    
    @Column(name = "weight")
    private Double weight;
    
    @Column(name = "dimensions")
    private String dimensions;
    
    @Column(name = "material")
    private String material;
    
    @Column(name = "color")
    private String color;
    
    @Column(name = "country_of_origin")
    private String countryOfOrigin;
    
    @Column(name = "warranty_months")
    private Integer warrantyMonths;
    
    @Column(name = "is_oe_quality")
    private Boolean isOeQuality = false;
    
    @Column(name = "is_aftermarket")
    private Boolean isAftermarket = false;
    
    /**
     * 상품 이미지 URL (레거시 필드)
     * - 기존 파일 시스템 방식에서 사용하던 필드
     * - 마이그레이션 후에는 DB의 images 관계로 대체됨
     */
    @Column(name = "image_url")
    private String imageUrl;
    
    /**
     * 상품 이미지 목록 (일대다 관계)
     * - 하나의 상품은 여러 개의 이미지를 가질 수 있음
     * - 이미지가 삭제되면 상품은 유지됨 (orphanRemoval = false)
     */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore  // JSON 직렬화 시 제외 (용량이 크므로)
    private java.util.List<ProductImage> images = new java.util.ArrayList<>();
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}