package com.inventory.service;

import com.inventory.dto.ProductDto;
import com.inventory.entity.Product;
import com.inventory.repository.ProductImageRepository;
import com.inventory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductService {
    
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    
    public List<ProductDto> getAllProducts() {
        log.info("모든 제품 조회");
        return productRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public ProductDto getProductById(Long id) {
        log.info("제품 조회: {}", id);
        return productRepository.findById(id)
                .map(this::convertToDto)
                .orElse(null);
    }
    
    public ProductDto createProduct(ProductDto productDto) {
        log.info("제품 생성: {}", productDto.getName());
        Product product = convertToEntity(productDto);
        Product saved = productRepository.save(product);
        return convertToDto(saved);
    }
    
    public ProductDto updateProduct(Long id, ProductDto productDto) {
        log.info("제품 수정: {}", id);
        return productRepository.findById(id)
                .map(existing -> {
                    updateEntity(existing, productDto);
                    Product saved = productRepository.save(existing);
                    return convertToDto(saved);
                })
                .orElse(null);
    }
    
    public void deleteProduct(Long id) {
        log.info("제품 삭제: {}", id);
        productRepository.deleteById(id);
    }
    
    private ProductDto convertToDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setSku(product.getSku());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setCost(product.getCost());
        dto.setBarcode(product.getBarcode());
        dto.setQuantity(product.getQuantity());
        dto.setMinimumQuantity(product.getMinimumQuantity());
        dto.setIsActive(product.getIsActive());
        dto.setIsSerialized(product.getIsSerialized());
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }
        
        // 이미지 정보 추가
        dto.setImageUrl(product.getImageUrl()); // 레거시 이미지 URL
        long imageCount = productImageRepository.countByProductId(product.getId());
        dto.setImageCount((int) imageCount);
        if (imageCount > 0) {
            // 최신 등록용 이미지를 대표로 노출 (없으면 기존 대표로 대체하는 것은 추후 확장)
            dto.setFirstImageUrl("/api/images/" + product.getId() + "/registration/last/data");
        }
        
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        return dto;
    }
    
    private Product convertToEntity(ProductDto dto) {
        Product product = new Product();
        product.setSku(dto.getSku());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setCost(dto.getCost());
        product.setBarcode(dto.getBarcode());
        product.setQuantity(dto.getQuantity());
        product.setMinimumQuantity(dto.getMinimumQuantity());
        product.setIsActive(dto.getIsActive());
        product.setIsSerialized(dto.getIsSerialized());
        return product;
    }
    
    private void updateEntity(Product product, ProductDto dto) {
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setCost(dto.getCost());
        product.setBarcode(dto.getBarcode());
        product.setQuantity(dto.getQuantity());
        product.setMinimumQuantity(dto.getMinimumQuantity());
        product.setIsActive(dto.getIsActive());
        product.setIsSerialized(dto.getIsSerialized());
    }
}

