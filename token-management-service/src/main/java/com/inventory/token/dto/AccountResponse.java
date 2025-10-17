package com.inventory.token.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {
    private String platform;
    private String username;
    private Boolean isActive;
    private LocalDateTime lastLogin;
    private Integer loginAttempts;
    private LocalDateTime lockedUntil;
    private LocalDateTime createdAt;
}

