package com.inventory.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlatformProductResponse {
    private String platform;
    private String externalProductId;
    private String status;
    private String message;
    private String errorMessage;
    private Long registrationId;
}

