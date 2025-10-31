package com.inventory.service;

import com.inventory.entity.ImageCategory;
import com.inventory.entity.Product;
import com.inventory.entity.ProductImage;
import com.inventory.repository.ProductImageRepository;
import com.inventory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * 상품 이미지 서비스
 * - 상품 이미지의 업로드, 조회, 삭제 기능 제공
 * - DB에 이미지 바이너리 데이터를 저장하고 관리
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductImageService {
    
    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;
    private final ImageResizeService imageResizeService;
    
    // 최대 이미지 크기 (10MB)
    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024;
    
    // 허용되는 이미지 타입
    private static final List<String> ALLOWED_IMAGE_TYPES = List.of(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );
    
    /**
     * 상품 이미지 업로드 (원본 + 리사이징된 버전들)
     * - MultipartFile을 받아서 원본, 썸네일, 등록용 이미지를 모두 생성하여 DB에 저장
     * 
     * @param productId 상품 ID
     * @param file 업로드할 이미지 파일
     * @return 저장된 원본 이미지 엔티티
     */
    public ProductImage uploadImage(Long productId, MultipartFile file) {
        log.info("이미지 업로드 시작 - 상품 ID: {}, 파일명: {}", productId, file.getOriginalFilename());
        
        // 1. 상품 존재 여부 확인
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + productId));
        
        // 2. 파일 유효성 검사
        validateImageFile(file);
        
        try {
            byte[] originalImageData = file.getBytes();
            String contentType = file.getContentType();
            String fileName = file.getOriginalFilename();
            
            // 3. 원본 이미지 저장
            ProductImage originalImage = createImageEntity(product, fileName, originalImageData, contentType, ImageCategory.ORIGINAL, null);
            ProductImage savedOriginal = productImageRepository.save(originalImage);
            
            log.info("원본 이미지 저장 완료 - 이미지 ID: {}, 크기: {} bytes", 
                    savedOriginal.getId(), savedOriginal.getImageSize());
            
            // 4. 등록용 이미지 생성 및 저장 (JPEG 800px/<=2MB)
            byte[] registrationImageData = imageResizeService.resizeForRegistration(originalImageData, contentType);
            ProductImage registrationImage = createImageEntity(product, ensureJpegName(fileName), registrationImageData, "image/jpeg", ImageCategory.REGISTRATION, savedOriginal.getId());
            productImageRepository.save(registrationImage);
            log.info("등록용 이미지 저장 완료 - 크기: {} bytes", registrationImageData.length);
            
            // 5. 썸네일 이미지 생성 및 저장 (JPEG)
            byte[] thumbnailImageData = imageResizeService.createThumbnail(originalImageData, contentType);
            ProductImage thumbnailImage = createImageEntity(product, ensureJpegName(fileName), thumbnailImageData, "image/jpeg", ImageCategory.THUMBNAIL, savedOriginal.getId());
            productImageRepository.save(thumbnailImage);
            log.info("썸네일 이미지 저장 완료 - 크기: {} bytes", thumbnailImageData.length);
            
            log.info("이미지 업로드 완료 - 원본 ID: {}, 총 크기: {} bytes", 
                    savedOriginal.getId(), originalImageData.length);
            
            return savedOriginal;
            
        } catch (IOException e) {
            log.error("이미지 파일 읽기 실패: {}", e.getMessage());
            throw new RuntimeException("이미지 파일을 읽을 수 없습니다", e);
        }
    }
    
    /**
     * ProductImage 엔티티 생성 헬퍼 메서드
     */
    private ProductImage createImageEntity(Product product, String fileName, byte[] imageData, 
                                         String contentType, ImageCategory category, Long originalImageId) {
        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setImageName(fileName);
        image.setImageData(imageData);
        image.setImageType(contentType);
        image.setImageSize((long) imageData.length);
        image.setImageCategory(category);
        image.setOriginalImageId(originalImageId);
        return image;
    }

    private String ensureJpegName(String name) {
        if (name == null || name.isBlank()) return "image.jpg";
        String lower = name.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return name;
        int dot = name.lastIndexOf('.');
        return (dot > 0 ? name.substring(0, dot) : name) + ".jpg";
    }
    
    /**
     * 상품의 모든 이미지 조회 (메타데이터만)
     * - 이미지 데이터는 제외하고 메타데이터만 조회
     * 
     * @param productId 상품 ID
     * @return 이미지 목록 (이미지 데이터 제외)
     */
    @Transactional(readOnly = true)
    public List<ProductImage> getImagesByProductId(Long productId) {
        log.info("상품 이미지 목록 조회 - 상품 ID: {}", productId);
        
        // 상품 존재 여부 확인
        if (!productRepository.existsById(productId)) {
            throw new IllegalArgumentException("상품을 찾을 수 없습니다: " + productId);
        }
        
        return productImageRepository.findByProductIdOrderByCreatedAtAsc(productId);
    }
    
    /**
     * 상품의 첫 번째 이미지 조회 (메타데이터만)
     * - 대표 이미지로 사용
     * 
     * @param productId 상품 ID
     * @return 첫 번째 이미지 (이미지 데이터 제외)
     */
    @Transactional(readOnly = true)
    public Optional<ProductImage> getFirstImageByProductId(Long productId) {
        log.info("상품 대표 이미지 조회 - 상품 ID: {}", productId);
        
        // 상품 존재 여부 확인
        if (!productRepository.existsById(productId)) {
            throw new IllegalArgumentException("상품을 찾을 수 없습니다: " + productId);
        }
        
        return productImageRepository.findFirstByProductIdOrderByCreatedAtAsc(productId);
    }
    
    /**
     * 이미지 실제 데이터 조회
     * - 이미지 표시용으로 실제 바이너리 데이터 조회
     * 
     * @param imageId 이미지 ID
     * @return 이미지 엔티티 (이미지 데이터 포함)
     */
    @Transactional(readOnly = true)
    public Optional<ProductImage> getImageData(Long imageId) {
        log.info("이미지 데이터 조회 - 이미지 ID: {}", imageId);
        
        return productImageRepository.findById(imageId);
    }
    
    /**
     * 상품의 첫 번째 이미지 데이터 조회
     * - 상품 대표 이미지의 실제 데이터 조회
     * 
     * @param productId 상품 ID
     * @return 첫 번째 이미지의 실제 데이터
     */
    @Transactional(readOnly = true)
    public Optional<ProductImage> getFirstImageData(Long productId) {
        log.info("상품 대표 이미지 데이터 조회 - 상품 ID: {}", productId);
        
        return productImageRepository.findFirstByProductIdOrderByCreatedAtAsc(productId);
    }
    
    /**
     * 이미지 삭제
     * - 특정 이미지를 삭제
     * 
     * @param imageId 이미지 ID
     */
    public void deleteImage(Long imageId) {
        log.info("이미지 삭제 - 이미지 ID: {}", imageId);
        
        if (!productImageRepository.existsById(imageId)) {
            throw new IllegalArgumentException("이미지를 찾을 수 없습니다: " + imageId);
        }
        
        productImageRepository.deleteById(imageId);
        log.info("이미지 삭제 완료 - 이미지 ID: {}", imageId);
    }
    
    /**
     * 상품의 모든 이미지 삭제
     * - 상품에 등록된 모든 이미지를 삭제
     * 
     * @param productId 상품 ID
     */
    public void deleteAllImagesByProductId(Long productId) {
        log.info("상품의 모든 이미지 삭제 - 상품 ID: {}", productId);
        
        long count = productImageRepository.countByProductId(productId);
        productImageRepository.deleteByProductId(productId);
        
        log.info("상품의 모든 이미지 삭제 완료 - 상품 ID: {}, 삭제된 이미지 수: {}", productId, count);
    }
    
    /**
     * 이미지 파일 유효성 검사
     * - 파일 크기, 타입 등을 검사
     * 
     * @param file 검사할 파일
     */
    private void validateImageFile(MultipartFile file) {
        // 1. 파일이 비어있는지 확인
        if (file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다");
        }
        
        // 2. 파일 크기 확인
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException(
                String.format("파일 크기가 너무 큽니다. 최대 크기: %d MB", MAX_IMAGE_SIZE / (1024 * 1024))
            );
        }
        
        // 3. 파일 타입 확인
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                "지원되지 않는 파일 타입입니다. 허용된 타입: " + String.join(", ", ALLOWED_IMAGE_TYPES)
            );
        }
        
        // 4. 파일명 확인
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("파일명이 올바르지 않습니다");
        }
        
        log.debug("이미지 파일 유효성 검사 통과 - 파일명: {}, 크기: {} bytes, 타입: {}", 
                 originalFilename, file.getSize(), contentType);
    }
    
    /**
     * 상품의 이미지 개수 조회
     * - 상품에 등록된 이미지 개수 확인
     * 
     * @param productId 상품 ID
     * @return 이미지 개수
     */
    @Transactional(readOnly = true)
    public long getImageCountByProductId(Long productId) {
        return productImageRepository.countByProductId(productId);
    }
    
    /**
     * 상품의 등록용 이미지 조회
     * - 번개장터 등록에 최적화된 이미지 조회
     * 
     * @param productId 상품 ID
     * @return 등록용 이미지 목록
     */
    @Transactional(readOnly = true)
    public List<ProductImage> getRegistrationImagesByProductId(Long productId) {
        log.info("상품 등록용 이미지 조회 - 상품 ID: {}", productId);
        return productImageRepository.findByProductIdAndImageCategoryOrderByCreatedAtAsc(productId, ImageCategory.REGISTRATION);
    }
    
    /**
     * 상품의 첫 번째 등록용 이미지 조회
     * - 번개장터 등록 시 대표 이미지로 사용
     * 
     * @param productId 상품 ID
     * @return 첫 번째 등록용 이미지
     */
    @Transactional(readOnly = true)
    public Optional<ProductImage> getFirstRegistrationImageByProductId(Long productId) {
        log.info("상품 첫 번째 등록용 이미지 조회 - 상품 ID: {}", productId);
        return productImageRepository.findFirstByProductIdAndImageCategoryOrderByCreatedAtAsc(productId, ImageCategory.REGISTRATION);
    }
    
    /**
     * 상품의 최신 등록용 이미지 조회
     * - 대표 이미지를 최신 REGISTRATION으로 사용하기 위함
     */
    @Transactional(readOnly = true)
    public Optional<ProductImage> getLatestRegistrationImageByProductId(Long productId) {
        log.info("상품 최신 등록용 이미지 조회 - 상품 ID: {}", productId);
        return productImageRepository.findFirstByProductIdAndImageCategoryOrderByCreatedAtDesc(productId, ImageCategory.REGISTRATION);
    }
    
    /**
     * 상품의 썸네일 이미지 조회
     * - 목록 표시용 작은 이미지 조회
     * 
     * @param productId 상품 ID
     * @return 썸네일 이미지 목록
     */
    @Transactional(readOnly = true)
    public List<ProductImage> getThumbnailImagesByProductId(Long productId) {
        log.info("상품 썸네일 이미지 조회 - 상품 ID: {}", productId);
        return productImageRepository.findByProductIdAndImageCategoryOrderByCreatedAtAsc(productId, ImageCategory.THUMBNAIL);
    }
    
    /**
     * 상품의 첫 번째 썸네일 이미지 조회
     * - 목록 표시용 대표 썸네일
     * 
     * @param productId 상품 ID
     * @return 첫 번째 썸네일 이미지
     */
    @Transactional(readOnly = true)
    public Optional<ProductImage> getFirstThumbnailImageByProductId(Long productId) {
        log.info("상품 첫 번째 썸네일 이미지 조회 - 상품 ID: {}", productId);
        return productImageRepository.findFirstByProductIdAndImageCategoryOrderByCreatedAtAsc(productId, ImageCategory.THUMBNAIL);
    }
    
    /**
     * 이미지 통계 정보 조회
     * - 전체 이미지 수, 총 용량 등 통계 정보
     * 
     * @return 이미지 통계 정보
     */
    @Transactional(readOnly = true)
    public ImageStatistics getImageStatistics() {
        List<ProductImage> allImages = productImageRepository.findAll();
        
        long totalImages = allImages.size();
        long totalSize = allImages.stream()
            .mapToLong(ProductImage::getImageSize)
            .sum();
        
        return new ImageStatistics(totalImages, totalSize);
    }
    
    /**
     * 이미지 통계 정보 클래스
     */
    public static class ImageStatistics {
        private final long totalImages;
        private final long totalSize;
        
        public ImageStatistics(long totalImages, long totalSize) {
            this.totalImages = totalImages;
            this.totalSize = totalSize;
        }
        
        public long getTotalImages() { return totalImages; }
        public long getTotalSize() { return totalSize; }
        public double getTotalSizeMB() { return totalSize / (1024.0 * 1024.0); }
    }
}
