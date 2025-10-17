package com.inventory.repository;

import com.inventory.entity.PartCondition;
import com.inventory.entity.PartType;
import com.inventory.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    Optional<Product> findBySku(String sku);
    
    Optional<Product> findByBarcode(String barcode);
    
    List<Product> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId")
    List<Product> findByCategoryId(@Param("categoryId") Long categoryId);
    
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice")
    List<Product> findByPriceBetween(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice);
    
    boolean existsBySku(String sku);
    
    boolean existsByBarcode(String barcode);
    
    // 대시보드 통계용 메서드들
    Long countByIsActiveTrue();
    Long countByIsSerializedTrue();
    
    // 카탈로그 검색 관련 메서드들
    List<Product> findByPartType(PartType partType);
    
    List<Product> findByPartCondition(PartCondition partCondition);
    
    List<Product> findByManufacturerCode(String manufacturerCode);
    
    List<Product> findByOemPartNumber(String oemPartNumber);
    
    List<Product> findByAftermarketPartNumber(String aftermarketPartNumber);
    
    @Query("SELECT p FROM Product p WHERE p.oemPartNumber LIKE %:partNumber% OR p.aftermarketPartNumber LIKE %:partNumber%")
    List<Product> findByPartNumberContaining(@Param("partNumber") String partNumber);
    
    @Query("SELECT p FROM Product p WHERE p.manufacturerName LIKE %:manufacturer%")
    List<Product> findByManufacturerNameContaining(@Param("manufacturer") String manufacturer);
    
    @Query("SELECT p FROM Product p WHERE p.isOeQuality = true")
    List<Product> findOeQualityProducts();
    
    @Query("SELECT p FROM Product p WHERE p.isAftermarket = true")
    List<Product> findAftermarketProducts();
}