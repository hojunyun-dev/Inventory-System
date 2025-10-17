package com.inventory.repository;

import com.inventory.entity.VehicleCompatibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleCompatibilityRepository extends JpaRepository<VehicleCompatibility, Long> {
    
    // 제조사로 검색
    List<VehicleCompatibility> findByManufacturerIgnoreCase(String manufacturer);
    
    // 제조사와 모델로 검색
    List<VehicleCompatibility> findByManufacturerIgnoreCaseAndModelIgnoreCase(String manufacturer, String model);
    
    // 특정 연식 범위에 해당하는 차량 검색
    @Query("SELECT v FROM VehicleCompatibility v WHERE v.yearStart <= :year AND (v.yearEnd IS NULL OR v.yearEnd >= :year)")
    List<VehicleCompatibility> findByYear(@Param("year") Integer year);
    
    // 제조사, 모델, 연식으로 검색
    @Query("SELECT v FROM VehicleCompatibility v WHERE " +
           "LOWER(v.manufacturer) = LOWER(:manufacturer) AND " +
           "LOWER(v.model) = LOWER(:model) AND " +
           "v.yearStart <= :year AND " +
           "(v.yearEnd IS NULL OR v.yearEnd >= :year)")
    List<VehicleCompatibility> findByManufacturerAndModelAndYear(
        @Param("manufacturer") String manufacturer,
        @Param("model") String model,
        @Param("year") Integer year
    );
    
    // 엔진 타입으로 검색
    List<VehicleCompatibility> findByEngineTypeContainingIgnoreCase(String engineType);
    
    // 연식 범위로 검색
    @Query("SELECT v FROM VehicleCompatibility v WHERE v.yearStart <= :year AND (v.yearEnd IS NULL OR v.yearEnd >= :year)")
    List<VehicleCompatibility> findByYearRange(@Param("year") Integer year);
}