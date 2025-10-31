package com.inventory.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class RegistrationResponse {
    private Long id;
    private String platform;
    private String productName;
    private String status;
    private String externalProductId;
    private String errorMessage;
    private Integer retryCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Map<String, String> registrationDetails;
}

