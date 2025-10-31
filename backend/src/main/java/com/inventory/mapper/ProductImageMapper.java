package com.inventory.mapper;

import com.inventory.dto.ProductImageDto;
import com.inventory.entity.ProductImage;
import org.springframework.stereotype.Component;

/**
 * ProductImage 엔티티와 DTO 간의 변환을 담당하는 매퍼
 * - 엔티티를 DTO로 변환할 때 이미지 바이너리 데이터는 제외
 * - API 응답 시 필요한 메타데이터만 포함
 */
@Component
public class ProductImageMapper {
    
    /**
     * ProductImage 엔티티를 ProductImageDto로 변환
     * - 이미지 바이너리 데이터는 제외하고 메타데이터만 변환
     * 
     * @param productImage 변환할 엔티티
     * @return 변환된 DTO
     */
    public ProductImageDto toDto(ProductImage productImage) {
        if (productImage == null) {
            return null;
        }
        
        return ProductImageDto.builder()
            .id(productImage.getId())
            .productId(productImage.getProduct() != null ? productImage.getProduct().getId() : null)
            .imageName(productImage.getImageName())
            .imageType(productImage.getImageType())
            .imageSize(productImage.getImageSize())
            .fileExtension(productImage.getFileExtension())
            .imageUrl(generateImageUrl(productImage.getId()))
            .createdAt(productImage.getCreatedAt())
            .updatedAt(productImage.getUpdatedAt())
            .isValid(productImage.isValid())
            .isImageType(productImage.isImageType())
            .build();
    }
    
    /**
     * ProductImage 엔티티를 ProductImageDto로 변환 (이미지 데이터 포함)
     * - 이미지 바이너리 데이터도 포함하여 변환
     * - 주의: 용량이 클 수 있으므로 신중하게 사용
     * 
     * @param productImage 변환할 엔티티
     * @return 변환된 DTO (이미지 데이터 포함)
     */
    public ProductImageDto toDtoWithData(ProductImage productImage) {
        if (productImage == null) {
            return null;
        }
        
        return ProductImageDto.builder()
            .id(productImage.getId())
            .productId(productImage.getProduct() != null ? productImage.getProduct().getId() : null)
            .imageName(productImage.getImageName())
            .imageType(productImage.getImageType())
            .imageSize(productImage.getImageSize())
            .fileExtension(productImage.getFileExtension())
            .imageUrl(generateImageUrl(productImage.getId()))
            .createdAt(productImage.getCreatedAt())
            .updatedAt(productImage.getUpdatedAt())
            .isValid(productImage.isValid())
            .isImageType(productImage.isImageType())
            .build();
    }
    
    /**
     * 이미지 ID로 이미지 URL 생성
     * - API 엔드포인트 URL 생성
     * 
     * @param imageId 이미지 ID
     * @return 이미지 URL
     */
    private String generateImageUrl(Long imageId) {
        if (imageId == null) {
            return null;
        }
        return "/api/images/" + imageId + "/data";
    }
    
    /**
     * 이미지 타입이 이미지인지 확인
     * - MIME 타입이 image/로 시작하는지 확인
     * 
     * @param mimeType MIME 타입
     * @return 이미지 타입 여부
     */
    public boolean isImageType(String mimeType) {
        return mimeType != null && mimeType.startsWith("image/");
    }
    
    /**
     * 파일 확장자 추출
     * - 파일명에서 확장자 추출
     * 
     * @param filename 파일명
     * @return 확장자 (소문자)
     */
    public String extractFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
    
    /**
     * 이미지 크기를 사람이 읽기 쉬운 형태로 포맷
     * - 바이트를 B, KB, MB, GB 단위로 변환
     * 
     * @param sizeInBytes 바이트 크기
     * @return 포맷된 크기 문자열
     */
    public String formatImageSize(Long sizeInBytes) {
        if (sizeInBytes == null) return "0 B";
        
        if (sizeInBytes < 1024) {
            return sizeInBytes + " B";
        } else if (sizeInBytes < 1024 * 1024) {
            return String.format("%.1f KB", sizeInBytes / 1024.0);
        } else if (sizeInBytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", sizeInBytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", sizeInBytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
}
