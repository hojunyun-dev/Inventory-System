package com.inventory.token.repository;

import com.inventory.token.entity.PlatformAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlatformAccountRepository extends JpaRepository<PlatformAccount, Long> {
    
    Optional<PlatformAccount> findByPlatformAndUsernameAndIsActiveTrue(String platform, String username);
    
    List<PlatformAccount> findByPlatformAndIsActiveTrue(String platform);
    
    List<PlatformAccount> findByIsActiveTrue();
    
    @Query("SELECT a FROM PlatformAccount a WHERE a.platform = :platform AND a.isActive = true AND (a.lockedUntil IS NULL OR a.lockedUntil < :now)")
    List<PlatformAccount> findUnlockedAccountsByPlatform(@Param("platform") String platform, @Param("now") LocalDateTime now);
    
    @Query("SELECT a FROM PlatformAccount a WHERE a.isActive = true AND a.lastLogin < :threshold")
    List<PlatformAccount> findInactiveAccounts(@Param("threshold") LocalDateTime threshold);
}

