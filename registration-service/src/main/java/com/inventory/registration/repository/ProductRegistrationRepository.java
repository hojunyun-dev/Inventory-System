package com.inventory.registration.repository;

import com.inventory.registration.entity.ProductRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRegistrationRepository extends JpaRepository<ProductRegistration, Long> {
    
    List<ProductRegistration> findByPlatform(String platform);
    
    List<ProductRegistration> findByPlatformAndStatus(String platform, String status);
    
    Optional<ProductRegistration> findByPlatformAndProductId(String platform, String productId);
    
    @Query("SELECT pr FROM ProductRegistration pr WHERE pr.platform = :platform AND pr.status IN ('PENDING', 'IN_PROGRESS')")
    List<ProductRegistration> findPendingRegistrationsByPlatform(@Param("platform") String platform);
    
    @Query("SELECT pr FROM ProductRegistration pr WHERE pr.status = 'PENDING' AND pr.retryCount < pr.maxRetries")
    List<ProductRegistration> findRetryableRegistrations();
    
    @Query("SELECT pr FROM ProductRegistration pr WHERE pr.platform = :platform AND pr.createdAt >= :since ORDER BY pr.createdAt DESC")
    List<ProductRegistration> findRecentRegistrationsByPlatform(@Param("platform") String platform, @Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(pr) FROM ProductRegistration pr WHERE pr.platform = :platform")
    Long countByPlatform(@Param("platform") String platform);
    
    @Query("SELECT COUNT(pr) FROM ProductRegistration pr WHERE pr.platform = :platform AND pr.status = :status")
    Long countByPlatformAndStatus(@Param("platform") String platform, @Param("status") String status);
}

