package com.inventory.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsDebugFilter extends OncePerRequestFilter {
    
    @Autowired(required = false)
    private CorsConfigurationSource source;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        if (CorsUtils.isCorsRequest(req) && source != null) {
            CorsConfiguration cfg = source.getCorsConfiguration(req);
            if (cfg != null) {
                System.out.printf("[CORS-DEBUG] origin=%s, allowCred=%s, allowedOrigins=%s, allowedPatterns=%s%n",
                        req.getHeader("Origin"), cfg.getAllowCredentials(), cfg.getAllowedOrigins(), cfg.getAllowedOriginPatterns());
            } else {
                System.out.println("[CORS-DEBUG] No CorsConfiguration matched this request");
            }
        }
        chain.doFilter(req, res);
    }
}

