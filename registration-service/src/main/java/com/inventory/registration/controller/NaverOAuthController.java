package com.inventory.registration.controller;

import com.inventory.registration.service.NaverOAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/oauth/naver")
@RequiredArgsConstructor
@Slf4j
public class NaverOAuthController {
    
    private final NaverOAuthService naverOAuthService;
    
    /**
     * 네이버 OAuth 인증 URL 생성
     */
    @GetMapping("/auth-url")
    public ResponseEntity<Map<String, String>> generateAuthUrl() {
        String state = UUID.randomUUID().toString();
        String authUrl = naverOAuthService.generateAuthUrl(state);
        
        log.info("네이버 OAuth 인증 URL 생성: {}", authUrl);
        
        return ResponseEntity.ok(Map.of(
                "authUrl", authUrl,
                "state", state
        ));
    }
    
    /**
     * OAuth 콜백 처리
     */
    @GetMapping("/callback")
    public Mono<ResponseEntity<Map<String, Object>>> handleCallback(
            @RequestParam String code,
            @RequestParam String state,
            @RequestParam(required = false) String error) {
        
        log.info("네이버 OAuth 콜백 처리 - code: {}, state: {}, error: {}", code, state, error);
        
        if (error != null) {
            return Mono.just(ResponseEntity.badRequest().body(Map.of(
                    "error", error,
                    "message", "OAuth 인증 실패"
            )));
        }
        
        return naverOAuthService.getAccessToken(code, state)
                .flatMap(tokenResponse -> {
                    String accessToken = (String) tokenResponse.get("access_token");
                    return naverOAuthService.getUserInfo(accessToken)
                            .map(userInfo -> Map.of(
                                    "accessToken", accessToken,
                                    "refreshToken", tokenResponse.get("refresh_token"),
                                    "expiresIn", tokenResponse.get("expires_in"),
                                    "tokenType", tokenResponse.get("token_type"),
                                    "userInfo", userInfo
                            ));
                })
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().body(Map.of(
                        "error", "TOKEN_ERROR",
                        "message", "토큰 발급 또는 사용자 정보 조회 실패"
                )));
    }
    
    /**
     * 사용자 정보 조회
     */
    @GetMapping("/user-info")
    public Mono<ResponseEntity<Map<String, Object>>> getUserInfo(
            @RequestHeader("Authorization") String authorization) {
        
        String accessToken = authorization.replace("Bearer ", "");
        log.info("네이버 사용자 정보 조회 요청");
        
        return naverOAuthService.getUserInfo(accessToken)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().body(Map.of(
                        "error", "USER_INFO_ERROR",
                        "message", "사용자 정보 조회 실패"
                )));
    }
    
    /**
     * 토큰 갱신
     */
    @PostMapping("/refresh")
    public Mono<ResponseEntity<Map<String, Object>>> refreshToken(
            @RequestParam String refreshToken) {
        
        log.info("네이버 토큰 갱신 요청");
        
        return naverOAuthService.refreshToken(refreshToken)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().body(Map.of(
                        "error", "REFRESH_ERROR",
                        "message", "토큰 갱신 실패"
                )));
    }
    
    /**
     * 토큰 폐기
     */
    @PostMapping("/revoke")
    public Mono<ResponseEntity<Map<String, Object>>> revokeToken(
            @RequestParam String accessToken) {
        
        log.info("네이버 토큰 폐기 요청");
        
        return naverOAuthService.revokeToken(accessToken)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().body(Map.of(
                        "error", "REVOKE_ERROR",
                        "message", "토큰 폐기 실패"
                )));
    }
}






