package com.inventory.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 상품 이미지 엔티티
 * - 상품과 연관된 이미지 데이터를 DB에 저장
 * - LONGBLOB 타입으로 실제 이미지 바이너리 데이터 저장
 */
@Entity
@Table(name = "product_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductImage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 연관된 상품 (다대일 관계)
     * - 상품이 삭제되면 관련 이미지도 함께 삭제 (CASCADE)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore  // JSON 직렬화 시 무한 루프 방지
    private Product product;
    
    /**
     * 원본 이미지 파일명
     * - 사용자가 업로드한 원본 파일명 저장
     */
    @Column(name = "image_name", nullable = false)
    private String imageName;
    
    /**
     * 실제 이미지 바이너리 데이터
     * - LONGBLOB 타입으로 최대 4GB까지 저장 가능
     * - JSON 직렬화 시 제외 (용량이 크므로)
     */
    @Lob
    @Column(name = "image_data", nullable = false, columnDefinition = "LONGBLOB")
    @JsonIgnore  // JSON 직렬화 시 제외 (용량이 크므로)
    private byte[] imageData;
    
    /**
     * 이미지 MIME 타입
     * - image/jpeg, image/png, image/gif 등
     */
    @Column(name = "image_type", nullable = false)
    private String imageType;
    
    /**
     * 이미지 파일 크기 (바이트)
     * - 파일 크기 제한 및 통계 분석용
     */
    @Column(name = "image_size", nullable = false)
    private Long imageSize;
    
    /**
     * 이미지 타입 구분
     * - ORIGINAL: 원본 이미지
     * - THUMBNAIL: 썸네일 이미지 (목록 표시용)
     * - REGISTRATION: 등록용 이미지 (번개장터 등록용)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "image_category", nullable = false)
    private ImageCategory imageCategory;
    
    /**
     * 원본 이미지 ID (리사이징된 이미지인 경우)
     * - 원본 이미지와의 연결을 위한 참조
     */
    @Column(name = "original_image_id")
    private Long originalImageId;
    
    /**
     * 생성 시간
     * - 이미지가 업로드된 시간
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 수정 시간
     * - 이미지 정보가 수정된 시간
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * 이미지가 유효한지 확인
     * - 이미지 데이터가 존재하고 크기가 0보다 큰지 확인
     */
    public boolean isValid() {
        return imageData != null && imageData.length > 0 && imageSize > 0;
    }
    
    /**
     * 이미지 확장자 반환
     * - 파일명에서 확장자 추출
     */
    public String getFileExtension() {
        if (imageName == null || !imageName.contains(".")) {
            return "";
        }
        return imageName.substring(imageName.lastIndexOf(".") + 1).toLowerCase();
    }
    
    /**
     * 이미지가 이미지 타입인지 확인
     * - MIME 타입이 image/로 시작하는지 확인
     */
    public boolean isImageType() {
        return imageType != null && imageType.startsWith("image/");
    }
    
    /**
     * 메타데이터만 포함하는 생성자 (이미지 데이터 제외)
     * - API 응답용으로 사용
     */
    public ProductImage(Long id, Product product, String imageName, String imageType, Long imageSize, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.product = product;
        this.imageName = imageName;
        this.imageType = imageType;
        this.imageSize = imageSize;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.imageData = null; // 이미지 데이터는 null로 설정
    }
}
