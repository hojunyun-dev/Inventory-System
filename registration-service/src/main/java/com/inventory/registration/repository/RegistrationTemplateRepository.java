package com.inventory.registration.repository;

import com.inventory.registration.entity.RegistrationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationTemplateRepository extends JpaRepository<RegistrationTemplate, Long> {
    
    List<RegistrationTemplate> findByPlatform(String platform);
    
    List<RegistrationTemplate> findByPlatformAndIsActiveTrue(String platform);
    
    Optional<RegistrationTemplate> findByPlatformAndTemplateName(String platform, String templateName);
    
    List<RegistrationTemplate> findByPlatformAndTemplateTypeAndIsActiveTrueOrderByPriorityAsc(String platform, String templateType);
}

