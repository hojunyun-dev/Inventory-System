package com.inventory.registration.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class RegistrationStatusResponse {
    private String platform;
    private String status;
    private Integer totalRegistrations;
    private Integer pendingCount;
    private Integer inProgressCount;
    private Integer successCount;
    private Integer failedCount;
    private LocalDateTime lastUpdated;
    private List<RegistrationSummary> recentRegistrations;
    
    @Data
    public static class RegistrationSummary {
        private Long id;
        private String productId;
        private String productName;
        private String status;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private String errorMessage;
    }
}

