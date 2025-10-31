package com.inventory.registration.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TokenResponse {
    private String platform;
    private String accessToken;
    private String tokenType;
    private String scope;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private Long expiresIn;
}

