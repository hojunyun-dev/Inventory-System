package com.inventory.controller;

import com.inventory.entity.*;
import com.inventory.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final VehicleCompatibilityRepository vehicleCompatibilityRepository;

    // 카테고리 계층 구조 조회
    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getCategories() {
        List<Category> categories = categoryRepository.findByIsActiveTrue();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/categories/root")
    public ResponseEntity<List<Category>> getRootCategories() {
        List<Category> rootCategories = categoryRepository.findByParentIsNullAndIsActiveTrue();
        return ResponseEntity.ok(rootCategories);
    }

    @GetMapping("/categories/{parentId}/children")
    public ResponseEntity<List<Category>> getCategoryChildren(@PathVariable Long parentId) {
        List<Category> children = categoryRepository.findChildrenByParentId(parentId);
        return ResponseEntity.ok(children);
    }

    // 부품 타입별 제품 조회
    @GetMapping("/products/by-type/{partType}")
    public ResponseEntity<List<Product>> getProductsByType(@PathVariable PartType partType) {
        List<Product> products = productRepository.findByPartType(partType);
        return ResponseEntity.ok(products);
    }

    // 제조사별 제품 조회
    @GetMapping("/products/by-manufacturer/{manufacturer}")
    public ResponseEntity<List<Product>> getProductsByManufacturer(@PathVariable String manufacturer) {
        List<Product> products = productRepository.findByManufacturerNameContaining(manufacturer);
        return ResponseEntity.ok(products);
    }

    // OE 품질 제품 조회
    @GetMapping("/products/oe-quality")
    public ResponseEntity<List<Product>> getOeQualityProducts() {
        List<Product> products = productRepository.findOeQualityProducts();
        return ResponseEntity.ok(products);
    }

    // 애프터마켓 제품 조회
    @GetMapping("/products/aftermarket")
    public ResponseEntity<List<Product>> getAftermarketProducts() {
        List<Product> products = productRepository.findAftermarketProducts();
        return ResponseEntity.ok(products);
    }

    // 부품번호로 검색
    @GetMapping("/products/search/part-number")
    public ResponseEntity<List<Product>> searchByPartNumber(@RequestParam String partNumber) {
        List<Product> products = productRepository.findByPartNumberContaining(partNumber);
        return ResponseEntity.ok(products);
    }

    // 고급 검색
    @GetMapping("/products/search/advanced")
    public ResponseEntity<List<Product>> advancedSearch(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) PartType partType,
            @RequestParam(required = false) PartCondition partCondition,
            @RequestParam(required = false) String manufacturer,
            @RequestParam(required = false) Boolean isOeQuality,
            @RequestParam(required = false) Boolean isAftermarket,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {
        
        List<Product> allProducts = productRepository.findAll();
        
        // 필터링 로직
        List<Product> filteredProducts = allProducts.stream()
                .filter(product -> keyword == null || 
                    product.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                    product.getDescription().toLowerCase().contains(keyword.toLowerCase()) ||
                    (product.getOemPartNumber() != null && product.getOemPartNumber().toLowerCase().contains(keyword.toLowerCase())) ||
                    (product.getAftermarketPartNumber() != null && product.getAftermarketPartNumber().toLowerCase().contains(keyword.toLowerCase())))
                .filter(product -> partType == null || product.getPartType() == partType)
                .filter(product -> partCondition == null || product.getPartCondition() == partCondition)
                .filter(product -> manufacturer == null || 
                    (product.getManufacturerName() != null && product.getManufacturerName().toLowerCase().contains(manufacturer.toLowerCase())))
                .filter(product -> isOeQuality == null || (product.getIsOeQuality() != null && product.getIsOeQuality().equals(isOeQuality)))
                .filter(product -> isAftermarket == null || (product.getIsAftermarket() != null && product.getIsAftermarket().equals(isAftermarket)))
                .filter(product -> minPrice == null || (product.getPrice() != null && product.getPrice().doubleValue() >= minPrice))
                .filter(product -> maxPrice == null || (product.getPrice() != null && product.getPrice().doubleValue() <= maxPrice))
                .toList();
        
        return ResponseEntity.ok(filteredProducts);
    }

    // 차량 호환성 검색
    @GetMapping("/vehicle-compatibility")
    public ResponseEntity<List<VehicleCompatibility>> getVehicleCompatibility(
            @RequestParam(required = false) String manufacturer,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) Integer year) {
        
        List<VehicleCompatibility> compatibilities;
        
        if (manufacturer != null && model != null && year != null) {
            compatibilities = vehicleCompatibilityRepository.findByManufacturerAndModelAndYear(manufacturer, model, year);
        } else if (manufacturer != null && model != null) {
            compatibilities = vehicleCompatibilityRepository.findByManufacturerIgnoreCaseAndModelIgnoreCase(manufacturer, model);
        } else if (manufacturer != null) {
            compatibilities = vehicleCompatibilityRepository.findByManufacturerIgnoreCase(manufacturer);
        } else {
            compatibilities = vehicleCompatibilityRepository.findAll();
        }
        
        return ResponseEntity.ok(compatibilities);
    }

    // 카탈로그 통계
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCatalogStats() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalProducts", productRepository.count());
        stats.put("activeProducts", productRepository.countByIsActiveTrue());
        stats.put("oeQualityProducts", productRepository.findOeQualityProducts().size());
        stats.put("aftermarketProducts", productRepository.findAftermarketProducts().size());
        stats.put("totalCategories", categoryRepository.count());
        stats.put("totalVehicleCompatibilities", vehicleCompatibilityRepository.count());
        
        // 부품 타입별 통계
        Map<String, Long> partTypeStats = new HashMap<>();
        for (PartType partType : PartType.values()) {
            partTypeStats.put(partType.name(), (long) productRepository.findByPartType(partType).size());
        }
        stats.put("partTypeStats", partTypeStats);
        
        return ResponseEntity.ok(stats);
    }

    // 부품 타입 목록
    @GetMapping("/part-types")
    public ResponseEntity<PartType[]> getPartTypes() {
        return ResponseEntity.ok(PartType.values());
    }

    // 부품 상태 목록
    @GetMapping("/part-conditions")
    public ResponseEntity<PartCondition[]> getPartConditions() {
        return ResponseEntity.ok(PartCondition.values());
    }
}