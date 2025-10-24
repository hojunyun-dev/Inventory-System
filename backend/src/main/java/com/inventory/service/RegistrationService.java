package com.inventory.service;

import com.inventory.dto.RegistrationRequest;
import com.inventory.dto.RegistrationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class RegistrationService {

    private final WebClient registrationServiceWebClient;
    
    public RegistrationService(@Qualifier("registrationServiceWebClient") WebClient registrationServiceWebClient) {
        this.registrationServiceWebClient = registrationServiceWebClient;
    }

    @Value("${registration-service.url:http://localhost:8082}")
    private String registrationServiceUrl;

    /**
     * 상품 등록 요청
     */
    public Mono<RegistrationResponse> registerProduct(RegistrationRequest request) {
        log.info("Registering product via registration service: {}", request.getProductName());
        
        return registrationServiceWebClient.post()
                .uri(registrationServiceUrl + "/api/automation/bunjang/register")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(RegistrationResponse.class)
                .doOnNext(response -> log.info("Successfully registered product: {}", request.getProductName()))
                .doOnError(e -> log.error("Failed to register product: {}", e.getMessage()));
    }

    /**
     * 등록 상태 조회
     */
    public Mono<RegistrationResponse> getRegistrationStatus(String platform) {
        log.info("Getting registration status for platform: {}", platform);
        
        return registrationServiceWebClient.get()
                .uri(registrationServiceUrl + "/api/registrations/status/" + platform)
                .retrieve()
                .bodyToMono(RegistrationResponse.class)
                .doOnNext(response -> log.info("Successfully retrieved registration status for platform: {}", platform))
                .doOnError(e -> log.error("Failed to get registration status for platform {}: {}", platform, e.getMessage()));
    }
}

