package com.inventory.token.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenRequest {
    @NotBlank(message = "Platform is required")
    private String platform;
    
    @NotBlank(message = "Client ID is required")
    private String clientId;
    
    @NotBlank(message = "Client Secret is required")
    private String clientSecret;
    
    private String scope;
    private String grantType = "client_credentials";
}

