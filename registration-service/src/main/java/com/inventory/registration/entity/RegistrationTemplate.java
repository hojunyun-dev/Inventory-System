package com.inventory.registration.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "registration_templates")
@Data
public class RegistrationTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String platform;

    @Column(nullable = false)
    private String templateName;

    @Column(columnDefinition = "TEXT")
    private String templateData; // JSON 형태의 템플릿 데이터

    @Column(nullable = false)
    private String templateType; // API, AUTOMATION

    private Boolean isActive = true;
    private Integer priority = 0; // 우선순위

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

