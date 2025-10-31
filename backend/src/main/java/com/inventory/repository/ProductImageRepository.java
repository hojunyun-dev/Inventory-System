package com.inventory.repository;

import com.inventory.entity.ImageCategory;
import com.inventory.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 상품 이미지 Repository
 * - ProductImage 엔티티의 데이터 접근을 담당
 * - 상품별 이미지 조회, 이미지 데이터 조회 등 제공
 */
@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    
    /**
     * 상품 ID로 이미지 목록 조회
     * - 상품에 등록된 모든 이미지를 조회
     * 
     * @param productId 상품 ID
     * @return 이미지 목록
     */
    List<ProductImage> findByProductIdOrderByCreatedAtAsc(Long productId);
    
    /**
     * 상품 ID로 첫 번째 이미지 조회
     * - 상품의 대표 이미지로 사용
     * 
     * @param productId 상품 ID
     * @return 첫 번째 이미지
     */
    Optional<ProductImage> findFirstByProductIdOrderByCreatedAtAsc(Long productId);
    
    /**
     * 상품 ID로 이미지 개수 조회
     * - 상품에 등록된 이미지 개수 확인
     * 
     * @param productId 상품 ID
     * @return 이미지 개수
     */
    long countByProductId(Long productId);
    
    /**
     * 상품 ID로 모든 이미지 삭제
     * - 상품 삭제 시 관련 이미지들도 함께 삭제
     * 
     * @param productId 상품 ID
     */
    void deleteByProductId(Long productId);
    
    /**
     * 상품 ID와 이미지 카테고리로 이미지 목록 조회
     * - 특정 카테고리의 이미지만 조회
     * 
     * @param productId 상품 ID
     * @param imageCategory 이미지 카테고리
     * @return 이미지 목록
     */
    List<ProductImage> findByProductIdAndImageCategoryOrderByCreatedAtAsc(Long productId, ImageCategory imageCategory);
    
    /**
     * 상품 ID와 이미지 카테고리로 첫 번째 이미지 조회
     * - 특정 카테고리의 첫 번째 이미지 조회
     * 
     * @param productId 상품 ID
     * @param imageCategory 이미지 카테고리
     * @return 첫 번째 이미지
     */
    Optional<ProductImage> findFirstByProductIdAndImageCategoryOrderByCreatedAtAsc(Long productId, ImageCategory imageCategory);
    Optional<ProductImage> findFirstByProductIdAndImageCategoryOrderByCreatedAtDesc(Long productId, ImageCategory imageCategory);
    
    /**
     * 상품 ID와 이미지 카테고리로 이미지 개수 조회
     * - 특정 카테고리의 이미지 개수 확인
     * 
     * @param productId 상품 ID
     * @param imageCategory 이미지 카테고리
     * @return 이미지 개수
     */
    long countByProductIdAndImageCategory(Long productId, ImageCategory imageCategory);
}