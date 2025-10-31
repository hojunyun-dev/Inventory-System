package com.inventory.controller;

import com.inventory.dto.ProductDto;
import com.inventory.entity.Product;
import com.inventory.entity.ProductImage;
import com.inventory.repository.ProductImageRepository;
import com.inventory.repository.ProductRepository;
import com.inventory.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {
    
    private final ProductService productService;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    
    @GetMapping
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        List<ProductDto> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable("id") Long id) {
        ProductDto product = productService.getProductById(id);
        if (product != null) {
            return ResponseEntity.ok(product);
        }
        return ResponseEntity.notFound().build();
    }
    
    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@RequestBody ProductDto productDto) {
        ProductDto created = productService.createProduct(productDto);
        return ResponseEntity.ok(created);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(@PathVariable("id") Long id, @RequestBody ProductDto productDto) {
        ProductDto updated = productService.updateProduct(id, productDto);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable("id") Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 상품 이미지 업로드
     */
    @PostMapping("/{productId}/images/upload")
    public ResponseEntity<Map<String, Object>> uploadImage(
            @PathVariable("productId") Long productId,
            @RequestParam("file") MultipartFile file) {
        
        log.info("이미지 업로드 요청 - 상품 ID: {}, 파일명: {}", productId, file.getOriginalFilename());
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 1. 상품 존재 여부 확인
            Optional<Product> productOptional = productRepository.findById(productId);
            if (productOptional.isEmpty()) {
                response.put("success", false);
                response.put("message", "상품을 찾을 수 없습니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            Product product = productOptional.get();
            
            // 2. 이미지 엔티티 생성 및 저장
            ProductImage productImage = new ProductImage();
            productImage.setProduct(product);
            productImage.setImageName(file.getOriginalFilename());
            productImage.setImageType(file.getContentType());
            productImage.setImageSize(file.getSize());
            productImage.setImageData(file.getBytes());
            productImage.setCreatedAt(LocalDateTime.now());
            productImage.setUpdatedAt(LocalDateTime.now());
            
            ProductImage savedImage = productImageRepository.save(productImage);
            
            response.put("success", true);
            response.put("message", "이미지 업로드 성공");
            response.put("imageId", savedImage.getId());
            response.put("imageName", savedImage.getImageName());
            response.put("imageSize", savedImage.getImageSize());
            
            log.info("이미지 업로드 성공 - 이미지 ID: {}, 파일명: {}", savedImage.getId(), savedImage.getImageName());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("이미지 업로드 실패 - 상품 ID: {}, 오류: {}", productId, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "이미지 업로드 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 상품 이미지 목록 조회
     */
    @GetMapping("/{productId}/images")
    public ResponseEntity<Map<String, Object>> getProductImages(@PathVariable("productId") Long productId) {
        log.info("상품 이미지 목록 조회 - 상품 ID: {}", productId);
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 1. 상품 존재 여부 확인
            Optional<Product> productOptional = productRepository.findById(productId);
            if (productOptional.isEmpty()) {
                response.put("success", false);
                response.put("message", "상품을 찾을 수 없습니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 2. 이미지 목록 조회
            List<ProductImage> images = productImageRepository.findByProductIdOrderByCreatedAtAsc(productId);
            
            response.put("success", true);
            response.put("count", images.size());
            response.put("images", images);
            
            log.info("상품 이미지 목록 조회 완료 - 상품 ID: {}, 이미지 수: {}", productId, images.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("상품 이미지 목록 조회 실패 - 상품 ID: {}, 오류: {}", productId, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "이미지 목록 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}

