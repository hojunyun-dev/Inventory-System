package com.example.common.dto;

import java.time.Instant;
import java.util.List;

public class TokenBundle {
    public String platform;            // "BUNJANG"
    public List<CookieEntry> cookies;  // name=value ...
    public String csrf;                // nullable
    public Instant expiresAt;          // nullable

    public TokenBundle() {}
    
    public TokenBundle(String platform, List<CookieEntry> cookies, String csrf, Instant expiresAt) {
        this.platform = platform; 
        this.cookies = cookies; 
        this.csrf = csrf; 
        this.expiresAt = expiresAt;
    }
}


