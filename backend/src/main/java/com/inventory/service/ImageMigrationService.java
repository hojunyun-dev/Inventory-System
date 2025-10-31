package com.inventory.service;

import com.inventory.entity.Product;
import com.inventory.entity.ProductImage;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.ProductImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

/**
 * 이미지 마이그레이션 서비스
 * - 기존 파일 시스템의 이미지를 DB로 마이그레이션
 * - 상품의 imageUrl 필드를 기반으로 파일을 찾아서 DB에 저장
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ImageMigrationService {
    
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    
    // 기존 이미지 파일이 저장된 디렉토리
    private static final String UPLOAD_DIR = "uploads/images/";
    
    /**
     * 모든 상품의 이미지를 파일 시스템에서 DB로 마이그레이션
     * 
     * @return 마이그레이션 결과 통계
     */
    public MigrationResult migrateAllImages() {
        log.info("🔄 이미지 마이그레이션 시작...");
        
        MigrationResult result = new MigrationResult();
        
        try {
            // 1. 모든 상품 조회
            List<Product> products = productRepository.findAll();
            log.info("📦 마이그레이션할 상품 수: {}", products.size());
            
            // 2. 각 상품의 이미지 마이그레이션
            for (Product product : products) {
                try {
                    migrateProductImages(product, result);
                } catch (Exception e) {
                    log.error("❌ 상품 이미지 마이그레이션 실패 - 상품 ID: {}, 오류: {}", 
                            product.getId(), e.getMessage());
                    result.addError(product.getId(), e.getMessage());
                }
            }
            
            log.info("✅ 이미지 마이그레이션 완료 - 성공: {}, 실패: {}, 총 처리: {}", 
                    result.getSuccessCount(), result.getErrorCount(), result.getTotalCount());
            
        } catch (Exception e) {
            log.error("❌ 이미지 마이그레이션 중 전체 오류 발생: {}", e.getMessage(), e);
            result.setErrorMessage("마이그레이션 중 오류 발생: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 특정 상품의 이미지 마이그레이션
     * 
     * @param product 마이그레이션할 상품
     * @param result 마이그레이션 결과 (통계용)
     */
    private void migrateProductImages(Product product, MigrationResult result) {
        log.debug("🔄 상품 이미지 마이그레이션 - 상품 ID: {}, 상품명: {}", 
                product.getId(), product.getName());
        
        // 1. 상품에 이미지가 이미 DB에 있는지 확인
        long existingImageCount = productImageRepository.countByProductId(product.getId());
        if (existingImageCount > 0) {
            log.debug("⏭️ 상품 ID {}는 이미 DB에 이미지가 있음 ({}개), 건너뜀", 
                    product.getId(), existingImageCount);
            result.addSkipped(product.getId(), "이미 DB에 이미지가 존재함");
            return;
        }
        
        // 2. 상품의 imageUrl 필드 확인
        String imageUrl = product.getImageUrl();
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            log.debug("⏭️ 상품 ID {}는 imageUrl이 없음, 건너뜀", product.getId());
            result.addSkipped(product.getId(), "imageUrl이 없음");
            return;
        }
        
        // 3. 파일 경로 생성
        String filePath = getFilePathFromUrl(imageUrl);
        if (filePath == null) {
            log.debug("⏭️ 상품 ID {}의 imageUrl이 올바르지 않음: {}", product.getId(), imageUrl);
            result.addSkipped(product.getId(), "올바르지 않은 imageUrl: " + imageUrl);
            return;
        }
        
        // 4. 파일 존재 여부 확인
        File imageFile = new File(filePath);
        if (!imageFile.exists()) {
            log.debug("⏭️ 상품 ID {}의 이미지 파일이 존재하지 않음: {}", product.getId(), filePath);
            result.addSkipped(product.getId(), "파일이 존재하지 않음: " + filePath);
            return;
        }
        
        // 5. 이미지 파일을 DB에 저장
        try {
            ProductImage productImage = createProductImageFromFile(product, imageFile);
            productImageRepository.save(productImage);
            
            log.info("✅ 상품 이미지 마이그레이션 성공 - 상품 ID: {}, 파일: {}", 
                    product.getId(), imageFile.getName());
            result.addSuccess(product.getId(), imageFile.getName());
            
        } catch (Exception e) {
            log.error("❌ 상품 이미지 저장 실패 - 상품 ID: {}, 파일: {}, 오류: {}", 
                    product.getId(), filePath, e.getMessage());
            result.addError(product.getId(), "파일 저장 실패: " + e.getMessage());
        }
    }
    
    /**
     * imageUrl에서 실제 파일 경로 추출
     * 
     * @param imageUrl 상품의 imageUrl 필드
     * @return 실제 파일 경로
     */
    private String getFilePathFromUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return null;
        }
        
        // URL에서 파일명만 추출
        String fileName;
        if (imageUrl.startsWith("/uploads/images/")) {
            fileName = imageUrl.substring("/uploads/images/".length());
        } else if (imageUrl.startsWith("uploads/images/")) {
            fileName = imageUrl.substring("uploads/images/".length());
        } else if (imageUrl.contains("/")) {
            fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        } else {
            fileName = imageUrl;
        }
        
        // 파일명이 비어있으면 null 반환
        if (fileName.trim().isEmpty()) {
            return null;
        }
        
        return UPLOAD_DIR + fileName;
    }
    
    /**
     * 파일로부터 ProductImage 엔티티 생성
     * 
     * @param product 상품 엔티티
     * @param imageFile 이미지 파일
     * @return ProductImage 엔티티
     */
    private ProductImage createProductImageFromFile(Product product, File imageFile) throws IOException {
        // 1. 파일 데이터 읽기
        byte[] imageData = Files.readAllBytes(imageFile.toPath());
        
        // 2. MIME 타입 추정
        String mimeType = getMimeTypeFromFile(imageFile);
        
        // 3. ProductImage 엔티티 생성
        ProductImage productImage = new ProductImage();
        productImage.setProduct(product);
        productImage.setImageName(imageFile.getName());
        productImage.setImageData(imageData);
        productImage.setImageType(mimeType);
        productImage.setImageSize(imageFile.length());
        
        return productImage;
    }
    
    /**
     * 파일 확장자로부터 MIME 타입 추정
     * 
     * @param file 파일
     * @return MIME 타입
     */
    private String getMimeTypeFromFile(File file) {
        String fileName = file.getName().toLowerCase();
        
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (fileName.endsWith(".png")) {
            return "image/png";
        } else if (fileName.endsWith(".gif")) {
            return "image/gif";
        } else if (fileName.endsWith(".webp")) {
            return "image/webp";
        } else if (fileName.endsWith(".bmp")) {
            return "image/bmp";
        } else {
            return "image/jpeg"; // 기본값
        }
    }
    
    /**
     * 마이그레이션 결과 클래스
     */
    public static class MigrationResult {
        private int totalCount = 0;
        private int successCount = 0;
        private int errorCount = 0;
        private int skippedCount = 0;
        private String errorMessage;
        private java.util.List<String> errors = new java.util.ArrayList<>();
        private java.util.List<String> skipped = new java.util.ArrayList<>();
        
        public void addSuccess(Long productId, String fileName) {
            successCount++;
            totalCount++;
            log.debug("✅ 마이그레이션 성공 - 상품 ID: {}, 파일: {}", productId, fileName);
        }
        
        public void addError(Long productId, String error) {
            errorCount++;
            totalCount++;
            errors.add("상품 ID " + productId + ": " + error);
        }
        
        public void addSkipped(Long productId, String reason) {
            skippedCount++;
            totalCount++;
            skipped.add("상품 ID " + productId + ": " + reason);
        }
        
        // Getters
        public int getTotalCount() { return totalCount; }
        public int getSuccessCount() { return successCount; }
        public int getErrorCount() { return errorCount; }
        public int getSkippedCount() { return skippedCount; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public java.util.List<String> getErrors() { return errors; }
        public java.util.List<String> getSkipped() { return skipped; }
        
        public boolean isSuccess() {
            return errorMessage == null && errorCount == 0;
        }
    }
}
