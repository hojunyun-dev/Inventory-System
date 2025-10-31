package com.inventory.token.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "platform_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlatformToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String platform; // naver, cafe24, coupang
    
    @Column(name = "access_token", columnDefinition = "TEXT")
    private String accessToken;
    
    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;
    
    @Column(name = "token_type")
    private String tokenType = "Bearer";
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "scope")
    private String scope;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

