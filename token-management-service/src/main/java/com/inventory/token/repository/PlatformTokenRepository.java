package com.inventory.token.repository;

import com.inventory.token.entity.PlatformToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlatformTokenRepository extends JpaRepository<PlatformToken, Long> {
    
    Optional<PlatformToken> findByPlatformAndIsActiveTrue(String platform);
    
    List<PlatformToken> findByIsActiveTrue();
    
    @Query("SELECT t FROM PlatformToken t WHERE t.platform = :platform AND t.expiresAt > :now AND t.isActive = true")
    Optional<PlatformToken> findValidTokenByPlatform(@Param("platform") String platform, @Param("now") LocalDateTime now);
    
    @Query("SELECT t FROM PlatformToken t WHERE t.expiresAt <= :threshold AND t.isActive = true")
    List<PlatformToken> findTokensExpiringSoon(@Param("threshold") LocalDateTime threshold);
    
    void deleteByPlatformAndIsActiveFalse(String platform);
}

