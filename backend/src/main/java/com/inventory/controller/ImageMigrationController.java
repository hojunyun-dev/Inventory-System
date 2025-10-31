package com.inventory.controller;

import com.inventory.service.ImageMigrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 이미지 마이그레이션 컨트롤러
 * - 기존 파일 시스템의 이미지를 DB로 마이그레이션하는 API 제공
 * - 관리자용 기능으로 사용
 */
@RestController
@RequestMapping("/api/admin/migration")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:3000")
public class ImageMigrationController {
    
    private final ImageMigrationService imageMigrationService;
    
    /**
     * 모든 이미지 마이그레이션 실행
     * - POST /api/admin/migration/images
     * 
     * @return 마이그레이션 결과
     */
    @PostMapping("/images")
    public ResponseEntity<?> migrateAllImages() {
        log.info("🔄 이미지 마이그레이션 요청 받음");
        
        try {
            ImageMigrationService.MigrationResult result = imageMigrationService.migrateAllImages();
            
            if (result.isSuccess()) {
                log.info("✅ 이미지 마이그레이션 성공 완료");
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "이미지 마이그레이션이 성공적으로 완료되었습니다",
                    "statistics", Map.of(
                        "totalCount", result.getTotalCount(),
                        "successCount", result.getSuccessCount(),
                        "errorCount", result.getErrorCount(),
                        "skippedCount", result.getSkippedCount()
                    ),
                    "errors", result.getErrors(),
                    "skipped", result.getSkipped()
                ));
            } else {
                log.warn("⚠️ 이미지 마이그레이션 완료 (일부 오류 포함)");
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "이미지 마이그레이션이 완료되었지만 일부 오류가 발생했습니다",
                    "errorMessage", result.getErrorMessage(),
                    "statistics", Map.of(
                        "totalCount", result.getTotalCount(),
                        "successCount", result.getSuccessCount(),
                        "errorCount", result.getErrorCount(),
                        "skippedCount", result.getSkippedCount()
                    ),
                    "errors", result.getErrors(),
                    "skipped", result.getSkipped()
                ));
            }
            
        } catch (Exception e) {
            log.error("❌ 이미지 마이그레이션 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "이미지 마이그레이션 중 오류가 발생했습니다",
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * 마이그레이션 상태 확인
     * - GET /api/admin/migration/status
     * 
     * @return 현재 마이그레이션 상태 정보
     */
    @GetMapping("/status")
    public ResponseEntity<?> getMigrationStatus() {
        log.info("📊 마이그레이션 상태 조회 요청");
        
        try {
            // TODO: 실제 구현 시 DB에서 마이그레이션 상태 조회
            // 현재는 간단한 상태 정보만 반환
            
            return ResponseEntity.ok(Map.of(
                "migrationAvailable", true,
                "message", "이미지 마이그레이션을 사용할 수 있습니다",
                "instructions", Map.of(
                    "step1", "POST /api/admin/migration/images 호출",
                    "step2", "마이그레이션 결과 확인",
                    "step3", "필요시 오류 수정 후 재실행"
                )
            ));
            
        } catch (Exception e) {
            log.error("❌ 마이그레이션 상태 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "마이그레이션 상태 조회 중 오류가 발생했습니다",
                "error", e.getMessage()
            ));
        }
    }
}
