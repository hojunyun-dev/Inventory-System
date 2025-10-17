package com.inventory.registration.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_registrations")
@Data
public class ProductRegistration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String platform;

    @Column(nullable = false)
    private String productId;

    @Column(nullable = false)
    private String productName;

    @Column(columnDefinition = "TEXT")
    private String productDescription;

    @Column(nullable = false)
    private String status; // PENDING, IN_PROGRESS, SUCCESS, FAILED

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(columnDefinition = "TEXT")
    private String platformProductId; // 플랫폼에서 생성된 상품 ID

    @Column(columnDefinition = "TEXT")
    private String platformUrl; // 플랫폼 상품 URL

    @Column(columnDefinition = "TEXT")
    private String requestData; // 요청 데이터 (JSON)

    @Column(columnDefinition = "TEXT")
    private String responseData; // 응답 데이터 (JSON)

    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer retryCount = 0;
    private Integer maxRetries = 3;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

