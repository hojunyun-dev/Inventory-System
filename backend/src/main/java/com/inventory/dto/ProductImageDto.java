package com.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 상품 이미지 DTO
 * - API 응답용으로 사용
 * - 이미지 바이너리 데이터는 제외하고 메타데이터만 포함
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageDto {
    
    /**
     * 이미지 ID
     */
    private Long id;
    
    /**
     * 상품 ID
     */
    private Long productId;
    
    /**
     * 이미지 파일명
     */
    private String imageName;
    
    /**
     * 이미지 MIME 타입
     */
    private String imageType;
    
    /**
     * 이미지 파일 크기 (바이트)
     */
    private Long imageSize;
    
    /**
     * 이미지 파일 확장자
     */
    private String fileExtension;
    
    /**
     * 이미지 URL (API 엔드포인트)
     */
    private String imageUrl;
    
    /**
     * 생성 시간
     */
    private LocalDateTime createdAt;
    
    /**
     * 수정 시간
     */
    private LocalDateTime updatedAt;
    
    /**
     * 이미지가 유효한지 여부
     */
    private Boolean isValid;
    
    /**
     * 이미지가 이미지 타입인지 여부
     */
    private Boolean isImageType;
    
    /**
     * 이미지 크기를 MB 단위로 반환
     */
    public Double getImageSizeMB() {
        if (imageSize == null) return 0.0;
        return imageSize / (1024.0 * 1024.0);
    }
    
    /**
     * 이미지 크기를 KB 단위로 반환
     */
    public Double getImageSizeKB() {
        if (imageSize == null) return 0.0;
        return imageSize / 1024.0;
    }
    
    /**
     * 이미지 크기를 사람이 읽기 쉬운 형태로 반환
     */
    public String getFormattedImageSize() {
        if (imageSize == null) return "0 B";
        
        if (imageSize < 1024) {
            return imageSize + " B";
        } else if (imageSize < 1024 * 1024) {
            return String.format("%.1f KB", getImageSizeKB());
        } else {
            return String.format("%.1f MB", getImageSizeMB());
        }
    }
}
