package com.inventory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle_compatibilities")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleCompatibility {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String manufacturer;
    
    @Column(nullable = false)
    private String model;
    
    @Column(name = "year_start")
    private Integer yearStart;
    
    @Column(name = "year_end")
    private Integer yearEnd;
    
    @Column(name = "engine_type")
    private String engineType;
    
    @Column(name = "transmission")
    private String transmission;
    
    @Column(name = "trim")
    private String trim;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
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

