package com.inventory.registration.service;

import com.inventory.registration.dto.ProductRegistrationRequest;
import com.inventory.registration.dto.ProductRegistrationResponse;
import com.inventory.registration.dto.RegistrationStatusResponse;
import com.inventory.registration.entity.ProductRegistration;
import com.inventory.registration.repository.ProductRegistrationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductRegistrationService {
    
    private final ProductRegistrationRepository registrationRepository;
    private final NaverRegistrationService naverService;
    private final Cafe24RegistrationService cafe24Service;
    private final CoupangRegistrationService coupangService;
    private final BunjangRegistrationService bunjangService;
    private final DanggeunRegistrationService danggeunService;
    
    @Async
    @Transactional
    public void registerProduct(ProductRegistrationRequest request) {
        log.info("Starting product registration for platform: {}, product: {}", 
            request.getPlatform(), request.getProductName());
        
        // Create registration record
        ProductRegistration registration = new ProductRegistration();
        registration.setPlatform(request.getPlatform());
        registration.setProductId(request.getProductId());
        registration.setProductName(request.getProductName());
        registration.setProductDescription(request.getProductDescription());
        registration.setStatus("PENDING");
        registration.setStartedAt(LocalDateTime.now());
        registration.setRequestData(convertToJson(request));
        
        registration = registrationRepository.save(registration);
        
        try {
            // Update status to IN_PROGRESS
            registration.setStatus("IN_PROGRESS");
            registration = registrationRepository.save(registration);
            
            // Route to appropriate service based on platform
            ProductRegistration result = routeToPlatformService(request);
            
            // Update registration with result
            registration.setStatus(result.getStatus());
            registration.setPlatformProductId(result.getPlatformProductId());
            registration.setPlatformUrl(result.getPlatformUrl());
            registration.setResponseData(convertToJson(result));
            registration.setCompletedAt(LocalDateTime.now());
            
            registration = registrationRepository.save(registration);
            
            log.info("Product registration completed for platform: {}, product: {}, status: {}", 
                request.getPlatform(), request.getProductName(), registration.getStatus());
            
        } catch (Exception e) {
            log.error("Product registration failed for platform: {}, product: {}, error: {}", 
                request.getPlatform(), request.getProductName(), e.getMessage(), e);
            
            registration.setStatus("FAILED");
            registration.setErrorMessage(e.getMessage());
            registration.setCompletedAt(LocalDateTime.now());
            registration = registrationRepository.save(registration);
        }
        
        // 비동기 처리이므로 void 반환
        log.info("Product registration completed for platform: {}, product: {}, status: {}", 
            request.getPlatform(), request.getProductName(), registration.getStatus());
    }
    
    private ProductRegistration routeToPlatformService(ProductRegistrationRequest request) {
        String platform = request.getPlatform().toLowerCase();
        
        return switch (platform) {
            case "naver" -> naverService.registerProduct(request);
            case "cafe24" -> cafe24Service.registerProduct(request);
            case "coupang" -> coupangService.registerProduct(request);
            case "bunjang" -> bunjangService.registerProduct(request);
            case "danggeun" -> danggeunService.registerProduct(request);
            default -> throw new IllegalArgumentException("Unsupported platform: " + platform);
        };
    }
    
    public List<ProductRegistrationResponse> getRegistrationsByPlatform(String platform) {
        List<ProductRegistration> registrations = registrationRepository.findByPlatform(platform);
        return registrations.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    public ProductRegistrationResponse getRegistrationById(Long id) {
        ProductRegistration registration = registrationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Registration not found with id: " + id));
        return convertToResponse(registration);
    }
    
    public RegistrationStatusResponse getRegistrationStatus(String platform) {
        List<ProductRegistration> registrations = registrationRepository.findByPlatform(platform);
        
        RegistrationStatusResponse status = new RegistrationStatusResponse();
        status.setPlatform(platform);
        status.setTotalRegistrations(registrations.size());
        status.setPendingCount(registrations.stream().mapToInt(r -> "PENDING".equals(r.getStatus()) ? 1 : 0).sum());
        status.setInProgressCount(registrations.stream().mapToInt(r -> "IN_PROGRESS".equals(r.getStatus()) ? 1 : 0).sum());
        status.setSuccessCount(registrations.stream().mapToInt(r -> "SUCCESS".equals(r.getStatus()) ? 1 : 0).sum());
        status.setFailedCount(registrations.stream().mapToInt(r -> "FAILED".equals(r.getStatus()) ? 1 : 0).sum());
        status.setLastUpdated(LocalDateTime.now());
        
        // Get recent registrations
        List<RegistrationStatusResponse.RegistrationSummary> recentRegistrations = registrations.stream()
            .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))
            .limit(10)
            .map(this::convertToSummary)
            .collect(Collectors.toList());
        status.setRecentRegistrations(recentRegistrations);
        
        return status;
    }
    
    public List<ProductRegistrationResponse> getRetryableRegistrations() {
        List<ProductRegistration> registrations = registrationRepository.findRetryableRegistrations();
        return registrations.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public ProductRegistrationResponse retryRegistration(Long id) {
        ProductRegistration registration = registrationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Registration not found with id: " + id));
        
        if (!"FAILED".equals(registration.getStatus())) {
            throw new RuntimeException("Only failed registrations can be retried");
        }
        
        if (registration.getRetryCount() >= registration.getMaxRetries()) {
            throw new RuntimeException("Maximum retry attempts exceeded");
        }
        
        registration.setRetryCount(registration.getRetryCount() + 1);
        registration.setStatus("PENDING");
        registration.setErrorMessage(null);
        registration.setStartedAt(LocalDateTime.now());
        registration.setCompletedAt(null);
        
        registration = registrationRepository.save(registration);
        
        // Trigger retry (this would typically be handled by a job scheduler)
        return convertToResponse(registration);
    }
    
    private ProductRegistrationResponse convertToResponse(ProductRegistration registration) {
        ProductRegistrationResponse response = new ProductRegistrationResponse();
        response.setId(registration.getId());
        response.setPlatform(registration.getPlatform());
        response.setProductId(registration.getProductId());
        response.setProductName(registration.getProductName());
        response.setStatus(registration.getStatus());
        response.setErrorMessage(registration.getErrorMessage());
        response.setPlatformProductId(registration.getPlatformProductId());
        response.setPlatformUrl(registration.getPlatformUrl());
        response.setStartedAt(registration.getStartedAt());
        response.setCompletedAt(registration.getCompletedAt());
        response.setRetryCount(registration.getRetryCount());
        response.setCreatedAt(registration.getCreatedAt());
        response.setUpdatedAt(registration.getUpdatedAt());
        
        // Parse response data if available
        if (registration.getResponseData() != null) {
            try {
                // This would need proper JSON parsing
                response.setResponseData(Map.of("data", registration.getResponseData()));
            } catch (Exception e) {
                log.warn("Failed to parse response data: {}", e.getMessage());
            }
        }
        
        return response;
    }
    
    private RegistrationStatusResponse.RegistrationSummary convertToSummary(ProductRegistration registration) {
        RegistrationStatusResponse.RegistrationSummary summary = new RegistrationStatusResponse.RegistrationSummary();
        summary.setId(registration.getId());
        summary.setProductId(registration.getProductId());
        summary.setProductName(registration.getProductName());
        summary.setStatus(registration.getStatus());
        summary.setStartedAt(registration.getStartedAt());
        summary.setCompletedAt(registration.getCompletedAt());
        summary.setErrorMessage(registration.getErrorMessage());
        return summary;
    }
    
    private String convertToJson(Object obj) {
        // This would need proper JSON serialization
        return obj.toString();
    }
}
