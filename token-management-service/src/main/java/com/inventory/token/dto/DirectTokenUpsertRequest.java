package com.inventory.token.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DirectTokenUpsertRequest {
    private String platform;
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private String scope;
    private Long expiresIn; // seconds
    private LocalDateTime expiresAt; // optional
}


