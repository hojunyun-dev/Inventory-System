package com.inventory.repository;

import com.inventory.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    List<Category> findByIsActiveTrue();
    
    List<Category> findByNameContainingIgnoreCase(String name);
    
    // 계층 구조 관련 메서드
    List<Category> findByParentIsNullAndIsActiveTrue();
    
    List<Category> findByParentIdAndIsActiveTrue(Long parentId);
    
    List<Category> findByLevelAndIsActiveTrue(Integer level);
    
    @Query("SELECT c FROM Category c WHERE c.parent.id = :parentId AND c.isActive = true ORDER BY c.sortOrder")
    List<Category> findChildrenByParentId(@Param("parentId") Long parentId);
    
    @Query("SELECT c FROM Category c WHERE c.level = :level AND c.isActive = true ORDER BY c.sortOrder")
    List<Category> findByLevelOrderBySortOrder(@Param("level") Integer level);
    
    List<Category> findByName(String name);
}