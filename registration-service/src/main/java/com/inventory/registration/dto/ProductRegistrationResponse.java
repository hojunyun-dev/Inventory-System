package com.inventory.registration.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class ProductRegistrationResponse {
    private Long id;
    private String platform;
    private String productId;
    private String productName;
    private String status;
    private String errorMessage;
    private String platformProductId;
    private String platformUrl;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer retryCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Map<String, Object> responseData;
}

