package com.inventory.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TokenResponse {
    private String platform;
    private String accessToken;
    private String tokenType;
    private String scope;
    private Long expiresIn;
    private String refreshToken;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}

