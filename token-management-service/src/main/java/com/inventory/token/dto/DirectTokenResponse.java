package com.inventory.token.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DirectTokenResponse {
    private String platform;
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private LocalDateTime expiresAt;
    private String scope;
    private Boolean isActive;
    private LocalDateTime createdAt;
}


