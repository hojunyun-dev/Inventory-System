package com.inventory.registration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutomationResult {
    
    // 실행 결과
    private boolean success;
    private String status; // SUCCESS, FAILED, PARTIAL_SUCCESS
    
    // 생성된 상품 정보
    private String productUrl;
    private String productId;
    private String externalId;
    
    // 에러 정보
    private String errorMessage;
    private String errorCode;
    private String errorDetails;
    
    // 실행 정보
    private String platform;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Long executionTimeMs;
    
    // 추가 메타데이터
    private Map<String, Object> metadata;
    private String screenshotPath; // 에러 발생 시 스크린샷 경로
    
    // 재시도 정보
    private int retryCount;
    private int maxRetries;
    
    // 편의 메서드
    public boolean isSuccessful() {
        return success && status != null && status.equals("SUCCESS");
    }
    
    public boolean hasError() {
        return !success || errorMessage != null;
    }
    
    public void markAsCompleted() {
        this.completedAt = LocalDateTime.now();
        if (this.startedAt != null) {
            this.executionTimeMs = java.time.Duration.between(this.startedAt, this.completedAt).toMillis();
        }
    }
    
    // Builder에서 사용할 수 있는 메서드
    public static class AutomationResultBuilder {
        public AutomationResultBuilder markAsCompleted() {
            this.completedAt = LocalDateTime.now();
            if (this.startedAt != null) {
                this.executionTimeMs = java.time.Duration.between(this.startedAt, this.completedAt).toMillis();
            }
            return this;
        }
    }
}
