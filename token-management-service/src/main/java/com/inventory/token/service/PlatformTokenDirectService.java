package com.inventory.token.service;

import com.inventory.token.dto.DirectTokenResponse;
import com.inventory.token.dto.DirectTokenUpsertRequest;
import com.inventory.token.entity.PlatformToken;
import com.inventory.token.repository.PlatformTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PlatformTokenDirectService {

    private final PlatformTokenRepository tokenRepository;

    public DirectTokenResponse upsert(DirectTokenUpsertRequest req) {
        if (req.getPlatform() == null || req.getPlatform().isBlank()) {
            throw new IllegalArgumentException("platform is required");
        }

        // deactivate existing active token for platform
        tokenRepository.findByPlatformAndIsActiveTrue(req.getPlatform())
            .ifPresent(t -> { t.setIsActive(false); tokenRepository.save(t); });

        PlatformToken token = new PlatformToken();
        token.setPlatform(req.getPlatform());
        token.setAccessToken(req.getAccessToken());
        token.setRefreshToken(req.getRefreshToken());
        token.setTokenType(req.getTokenType() != null ? req.getTokenType() : "Bearer");
        token.setScope(req.getScope());
        if (req.getExpiresAt() != null) {
            token.setExpiresAt(req.getExpiresAt());
        } else if (req.getExpiresIn() != null) {
            token.setExpiresAt(LocalDateTime.now().plusSeconds(req.getExpiresIn()));
        }
        token.setIsActive(true);

        PlatformToken saved = tokenRepository.save(token);
        return toResponse(saved);
    }

    public DirectTokenResponse get(String platform) {
        PlatformToken token = tokenRepository.findByPlatformAndIsActiveTrue(platform)
            .orElseThrow(() -> new RuntimeException("No active token for platform: " + platform));
        return toResponse(token);
    }

    private DirectTokenResponse toResponse(PlatformToken token) {
        DirectTokenResponse res = new DirectTokenResponse();
        res.setPlatform(token.getPlatform());
        res.setAccessToken(token.getAccessToken());
        res.setRefreshToken(token.getRefreshToken());
        res.setTokenType(token.getTokenType());
        res.setScope(token.getScope());
        res.setIsActive(token.getIsActive());
        res.setCreatedAt(token.getCreatedAt());
        if (token.getExpiresAt() != null) {
            res.setExpiresAt(token.getExpiresAt());
            res.setExpiresIn(java.time.Duration.between(LocalDateTime.now(), token.getExpiresAt()).getSeconds());
        }
        return res;
    }
}


