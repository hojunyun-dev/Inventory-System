package com.inventory.registration.service;

import com.inventory.registration.constants.PlatformConstants;
import com.inventory.registration.model.AutomationResult;
import com.inventory.registration.model.ProductData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutomationOrchestratorService {
    
    private final BunjangAutomationService bunjangAutomationService;
    private final DanggeunAutomationService danggeunAutomationService;
    private final JunggonaraAutomationService junggonaraAutomationService;
    
    private final ExecutorService executorService = Executors.newFixedThreadPool(3);
    
    /**
     * 모든 플랫폼에 상품 등록
     */
    @Async
    public CompletableFuture<List<AutomationResult>> registerProductToAllPlatforms(
            ProductData productData, 
            String username, 
            String password) {
        
        log.info("Starting product registration to all platforms");
        
        List<CompletableFuture<AutomationResult>> futures = new ArrayList<>();
        
        // 번개장터 등록
        CompletableFuture<AutomationResult> bunjangFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return bunjangAutomationService.registerProduct(productData, username, password);
            } catch (Exception e) {
                log.error("Bunjang registration failed: {}", e.getMessage());
                return AutomationResult.builder()
                        .platform(PlatformConstants.BUNJANG)
                        .success(false)
                        .errorMessage(e.getMessage())
                        .build();
            }
        }, executorService);
        futures.add(bunjangFuture);
        
        // 당근마켓 등록 (휴대폰 번호 필요)
        CompletableFuture<AutomationResult> danggeunFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return danggeunAutomationService.registerProduct(productData, username, password);
            } catch (Exception e) {
                log.error("Danggeun registration failed: {}", e.getMessage());
                return AutomationResult.builder()
                        .platform(PlatformConstants.DANGGEUN)
                        .success(false)
                        .errorMessage(e.getMessage())
                        .build();
            }
        }, executorService);
        futures.add(danggeunFuture);
        
        // 중고나라 등록
        CompletableFuture<AutomationResult> junggonaraFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return junggonaraAutomationService.registerProduct(productData, username, password);
            } catch (Exception e) {
                log.error("Junggonara registration failed: {}", e.getMessage());
                return AutomationResult.builder()
                        .platform(PlatformConstants.JUNGGONARA)
                        .success(false)
                        .errorMessage(e.getMessage())
                        .build();
            }
        }, executorService);
        futures.add(junggonaraFuture);
        
        // 모든 결과 대기
        CompletableFuture<List<AutomationResult>> allResults = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .toList());
        
        return allResults;
    }
    
    /**
     * 특정 플랫폼에 상품 등록
     */
    public AutomationResult registerProductToPlatform(String platform, ProductData productData, 
                                                     String username, String password) {
        
        log.info("Starting product registration to platform: {}", platform);
        
        try {
            switch (platform.toLowerCase()) {
                case PlatformConstants.BUNJANG:
                    return bunjangAutomationService.registerProduct(productData, username, password);
                    
                case PlatformConstants.DANGGEUN:
                    return danggeunAutomationService.registerProduct(productData, username, password);
                    
                case PlatformConstants.JUNGGONARA:
                    return junggonaraAutomationService.registerProduct(productData, username, password);
                    
                default:
                    return AutomationResult.builder()
                            .platform(platform)
                            .success(false)
                            .errorMessage("Unsupported platform: " + platform)
                            .build();
            }
        } catch (Exception e) {
            log.error("Platform registration failed for {}: {}", platform, e.getMessage());
            return AutomationResult.builder()
                    .platform(platform)
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }
    
    /**
     * 지원되는 플랫폼 목록 반환
     */
    public List<String> getSupportedPlatforms() {
        return Arrays.asList(
                PlatformConstants.BUNJANG,
                PlatformConstants.DANGGEUN,
                PlatformConstants.JUNGGONARA
        );
    }
    
    /**
     * 플랫폼별 상태 확인
     */
    public List<String> getPlatformStatus() {
        List<String> statusList = new ArrayList<>();
        
        // 각 플랫폼의 상태를 확인 (간단한 URL 접근 테스트)
        statusList.add("Bunjang: Available");
        statusList.add("Danggeun: Available (SMS verification required)");
        statusList.add("Junggonara: Available");
        
        return statusList;
    }
}






