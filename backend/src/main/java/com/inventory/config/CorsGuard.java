package com.inventory.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Map;

@Component
public class CorsGuard implements ApplicationRunner {
    
    private final ApplicationContext ctx;
    
    public CorsGuard(ApplicationContext ctx) { 
        this.ctx = ctx; 
    }

    @Override 
    public void run(ApplicationArguments args) {
        System.out.println("=== CORS Guard Check Start ===");
        
        var map = ctx.getBeansOfType(CorsConfigurationSource.class);
        for (var e : map.entrySet()) {
            try {
                CorsConfiguration cfg = e.getValue().getCorsConfiguration(null);
                if (cfg != null && Boolean.TRUE.equals(cfg.getAllowCredentials())) {
                    var origins = cfg.getAllowedOrigins();
                    if (origins != null && origins.contains("*")) {
                        System.err.println("BLOCKED: " + e.getKey() + " sets allowCredentials(true) with allowedOrigins(*)");
                        throw new IllegalStateException(
                            "Blocked: " + e.getKey() + " sets allowCredentials(true) with allowedOrigins(*)");
                    }
                }
                System.out.println("[CORS-GUARD] " + e.getKey() + " - OK");
            } catch (Exception ex) {
                System.out.println("[CORS-GUARD] " + e.getKey() + " - Error: " + ex.getMessage());
            }
        }
        
        System.out.println("=== CORS Guard Check End ===");
    }
}
