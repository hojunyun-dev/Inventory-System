package com.inventory.controller;

import com.inventory.entity.ProductImage;
import com.inventory.service.ProductImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 상품 이미지 컨트롤러
 * - 상품 이미지의 업로드, 조회, 삭제 API 제공
 * - RESTful API 설계에 따라 구현
 */
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:3000")
public class ProductImageController {
    
    private final ProductImageService productImageService;
    
    /**
     * 상품 이미지 업로드
     * - POST /api/images/{productId}
     * 
     * @param productId 상품 ID
     * @param file 업로드할 이미지 파일
     * @return 업로드된 이미지 정보
     */
    @PostMapping("/{productId}")
    public ResponseEntity<?> uploadImage(
            @PathVariable("productId") Long productId,
            @RequestParam("file") MultipartFile file) {
        
        log.info("이미지 업로드 요청 - 상품 ID: {}, 파일명: {}", productId, file.getOriginalFilename());
        
        try {
            ProductImage uploadedImage = productImageService.uploadImage(productId, file);
            
            // 이미지 데이터는 제외하고 메타데이터만 반환
            ProductImage responseImage = new ProductImage();
            responseImage.setId(uploadedImage.getId());
            responseImage.setImageName(uploadedImage.getImageName());
            responseImage.setImageType(uploadedImage.getImageType());
            responseImage.setImageSize(uploadedImage.getImageSize());
            responseImage.setCreatedAt(uploadedImage.getCreatedAt());
            responseImage.setUpdatedAt(uploadedImage.getUpdatedAt());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(responseImage);
            
        } catch (IllegalArgumentException e) {
            log.warn("이미지 업로드 실패 - 잘못된 요청: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            
        } catch (Exception e) {
            log.error("이미지 업로드 실패 - 서버 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "이미지 업로드 중 오류가 발생했습니다"));
        }
    }
    
    /**
     * 상품의 모든 이미지 조회 (메타데이터만)
     * - GET /api/products/{productId}/images
     * 
     * @param productId 상품 ID
     * @return 이미지 목록 (이미지 데이터 제외)
     */
    @GetMapping("/{productId}")
    public ResponseEntity<?> getImages(@PathVariable("productId") Long productId) {
        log.info("상품 이미지 목록 조회 - 상품 ID: {}", productId);
        
        try {
            List<ProductImage> images = productImageService.getImagesByProductId(productId);
            return ResponseEntity.ok(images);
            
        } catch (IllegalArgumentException e) {
            log.warn("이미지 목록 조회 실패 - 잘못된 요청: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            
        } catch (Exception e) {
            log.error("이미지 목록 조회 실패 - 서버 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "이미지 목록 조회 중 오류가 발생했습니다"));
        }
    }
    
    /**
     * 상품의 대표 이미지 조회 (메타데이터만)
     * - GET /api/products/{productId}/images/first
     * 
     * @param productId 상품 ID
     * @return 첫 번째 이미지 (이미지 데이터 제외)
     */
    @GetMapping("/{productId}/first")
    public ResponseEntity<?> getFirstImage(@PathVariable("productId") Long productId) {
        log.info("상품 대표 이미지 조회 - 상품 ID: {}", productId);
        
        try {
            Optional<ProductImage> image = productImageService.getFirstImageByProductId(productId);
            
            if (image.isPresent()) {
                return ResponseEntity.ok(image.get());
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (IllegalArgumentException e) {
            log.warn("대표 이미지 조회 실패 - 잘못된 요청: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            
        } catch (Exception e) {
            log.error("대표 이미지 조회 실패 - 서버 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "대표 이미지 조회 중 오류가 발생했습니다"));
        }
    }
    
    /**
     * 이미지 실제 데이터 조회
     * - GET /api/images/{imageId}/data
     * - 이미지 표시용으로 실제 바이너리 데이터 반환
     * 
     * @param imageId 이미지 ID
     * @return 이미지 바이너리 데이터
     */
    @GetMapping("/{imageId}/data")
    public ResponseEntity<?> getImageData(@PathVariable("imageId") Long imageId) {
        log.info("이미지 데이터 조회 - 이미지 ID: {}", imageId);
        
        try {
            Optional<ProductImage> image = productImageService.getImageData(imageId);
            
            if (image.isPresent()) {
                ProductImage imageData = image.get();
                
                // HTTP 헤더 설정
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(imageData.getImageType()));
                headers.setContentLength(imageData.getImageSize());
                headers.set(HttpHeaders.CONTENT_DISPOSITION, 
                    "inline; filename=\"" + imageData.getImageName() + "\"");
                
                return ResponseEntity.ok()
                    .headers(headers)
                    .body(imageData.getImageData());
                    
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("이미지 데이터 조회 실패 - 서버 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "이미지 데이터 조회 중 오류가 발생했습니다"));
        }
    }
    
    /**
     * 상품의 대표 이미지 데이터 조회
     * - GET /api/products/{productId}/images/first/data
     * - 상품 대표 이미지의 실제 바이너리 데이터 반환
     * 
     * @param productId 상품 ID
     * @return 대표 이미지 바이너리 데이터
     */
    @GetMapping("/{productId}/first/data")
    public ResponseEntity<?> getFirstImageData(@PathVariable("productId") Long productId) {
        log.info("상품 대표 이미지 데이터 조회 - 상품 ID: {}", productId);
        
        try {
            Optional<ProductImage> image = productImageService.getFirstImageData(productId);
            
            if (image.isPresent()) {
                ProductImage imageData = image.get();
                
                // HTTP 헤더 설정
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(imageData.getImageType()));
                headers.setContentLength(imageData.getImageSize());
                headers.set(HttpHeaders.CONTENT_DISPOSITION, 
                    "inline; filename=\"" + imageData.getImageName() + "\"");
                
                return ResponseEntity.ok()
                    .headers(headers)
                    .body(imageData.getImageData());
                    
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (IllegalArgumentException e) {
            log.warn("대표 이미지 데이터 조회 실패 - 잘못된 요청: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            
        } catch (Exception e) {
            log.error("대표 이미지 데이터 조회 실패 - 서버 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "대표 이미지 데이터 조회 중 오류가 발생했습니다"));
        }
    }
    
    /**
     * 상품의 등록용 이미지 데이터 조회
     * - 번개장터 등록에 최적화된 이미지 데이터 반환
     * 
     * @param productId 상품 ID
     * @return 등록용 이미지 바이너리 데이터
     */
    @GetMapping("/{productId}/registration/first/data")
    public ResponseEntity<?> getFirstRegistrationImageData(@PathVariable("productId") Long productId) {
        log.info("상품 등록용 대표 이미지 데이터 조회 - 상품 ID: {}", productId);
        
        try {
            Optional<ProductImage> image = productImageService.getFirstRegistrationImageByProductId(productId);
            
            if (image.isPresent()) {
                ProductImage imageData = image.get();
                
                // HTTP 헤더 설정
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(imageData.getImageType()));
                headers.setContentLength(imageData.getImageSize());
                headers.set(HttpHeaders.CONTENT_DISPOSITION, 
                    "inline; filename=\"" + imageData.getImageName() + "\"");
                
                return ResponseEntity.ok()
                    .headers(headers)
                    .body(imageData.getImageData());
                    
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (IllegalArgumentException e) {
            log.warn("등록용 대표 이미지 데이터 조회 실패 - 잘못된 요청: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            
        } catch (Exception e) {
            log.error("등록용 대표 이미지 데이터 조회 실패 - 서버 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "등록용 이미지 데이터 조회 중 오류가 발생했습니다"));
        }
    }
    
    /**
     * 상품의 최신 등록용 이미지 데이터 조회
     * - 목록 대표 이미지로 최신 REGISTRATION을 사용하고자 할 때
     */
    @GetMapping("/{productId}/registration/last/data")
    public ResponseEntity<?> getLatestRegistrationImageData(@PathVariable("productId") Long productId) {
        log.info("상품 최신 등록용 이미지 데이터 조회 - 상품 ID: {}", productId);
        try {
            Optional<ProductImage> image = productImageService.getLatestRegistrationImageByProductId(productId);
            if (image.isPresent()) {
                ProductImage imageData = image.get();
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(imageData.getImageType()));
                headers.setContentLength(imageData.getImageSize());
                headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + imageData.getImageName() + "\"");
                return ResponseEntity.ok().headers(headers).body(imageData.getImageData());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("최신 등록용 이미지 데이터 조회 실패 - 서버 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "최신 등록용 이미지 데이터 조회 중 오류가 발생했습니다"));
        }
    }
    
    /**
     * 상품의 썸네일 이미지 데이터 조회
     * - 목록 표시용 작은 이미지 데이터 반환
     * 
     * @param productId 상품 ID
     * @return 썸네일 이미지 바이너리 데이터
     */
    @GetMapping("/{productId}/thumbnail/first/data")
    public ResponseEntity<?> getFirstThumbnailImageData(@PathVariable("productId") Long productId) {
        log.info("상품 썸네일 대표 이미지 데이터 조회 - 상품 ID: {}", productId);
        
        try {
            Optional<ProductImage> image = productImageService.getFirstThumbnailImageByProductId(productId);
            
            if (image.isPresent()) {
                ProductImage imageData = image.get();
                
                // HTTP 헤더 설정
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(imageData.getImageType()));
                headers.setContentLength(imageData.getImageSize());
                headers.set(HttpHeaders.CONTENT_DISPOSITION, 
                    "inline; filename=\"" + imageData.getImageName() + "\"");
                
                return ResponseEntity.ok()
                    .headers(headers)
                    .body(imageData.getImageData());
                    
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (IllegalArgumentException e) {
            log.warn("썸네일 대표 이미지 데이터 조회 실패 - 잘못된 요청: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            
        } catch (Exception e) {
            log.error("썸네일 대표 이미지 데이터 조회 실패 - 서버 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "썸네일 이미지 데이터 조회 중 오류가 발생했습니다"));
        }
    }
    
    /**
     * 이미지 삭제
     * - DELETE /api/images/{imageId}
     * 
     * @param imageId 이미지 ID
     * @return 삭제 결과
     */
    @DeleteMapping("/{imageId}")
    public ResponseEntity<?> deleteImage(@PathVariable("imageId") Long imageId) {
        log.info("이미지 삭제 요청 - 이미지 ID: {}", imageId);
        
        try {
            productImageService.deleteImage(imageId);
            return ResponseEntity.ok(Map.of("message", "이미지가 삭제되었습니다"));
            
        } catch (IllegalArgumentException e) {
            log.warn("이미지 삭제 실패 - 잘못된 요청: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            
        } catch (Exception e) {
            log.error("이미지 삭제 실패 - 서버 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "이미지 삭제 중 오류가 발생했습니다"));
        }
    }
    
    /**
     * 상품의 모든 이미지 삭제
     * - DELETE /api/products/{productId}/images
     * 
     * @param productId 상품 ID
     * @return 삭제 결과
     */
    @DeleteMapping("/{productId}/all")
    public ResponseEntity<?> deleteAllImages(@PathVariable("productId") Long productId) {
        log.info("상품의 모든 이미지 삭제 요청 - 상품 ID: {}", productId);
        
        try {
            productImageService.deleteAllImagesByProductId(productId);
            return ResponseEntity.ok(Map.of("message", "상품의 모든 이미지가 삭제되었습니다"));
            
        } catch (Exception e) {
            log.error("상품의 모든 이미지 삭제 실패 - 서버 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "이미지 삭제 중 오류가 발생했습니다"));
        }
    }
    
    /**
     * 상품의 이미지 개수 조회
     * - GET /api/products/{productId}/images/count
     * 
     * @param productId 상품 ID
     * @return 이미지 개수
     */
    @GetMapping("/{productId}/count")
    public ResponseEntity<?> getImageCount(@PathVariable("productId") Long productId) {
        log.info("상품 이미지 개수 조회 - 상품 ID: {}", productId);
        
        try {
            long count = productImageService.getImageCountByProductId(productId);
            return ResponseEntity.ok(Map.of("count", count));
            
        } catch (Exception e) {
            log.error("이미지 개수 조회 실패 - 서버 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "이미지 개수 조회 중 오류가 발생했습니다"));
        }
    }
    
    /**
     * 이미지 통계 정보 조회
     * - GET /api/images/statistics
     * 
     * @return 이미지 통계 정보
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getImageStatistics() {
        log.info("이미지 통계 정보 조회");
        
        try {
            ProductImageService.ImageStatistics statistics = productImageService.getImageStatistics();
            return ResponseEntity.ok(statistics);
            
        } catch (Exception e) {
            log.error("이미지 통계 조회 실패 - 서버 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "이미지 통계 조회 중 오류가 발생했습니다"));
        }
    }
}
