package com.inventory.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Map;

@Component
public class CorsBeanDumper implements ApplicationRunner {
    
    private final ApplicationContext ctx;
    
    public CorsBeanDumper(ApplicationContext ctx) { 
        this.ctx = ctx; 
    }

    @Override 
    public void run(ApplicationArguments args) {
        System.out.println("=== CORS Bean Dump Start ===");
        
        // CorsConfigurationSource 빈들 확인
        Map<String, CorsConfigurationSource> sources = ctx.getBeansOfType(CorsConfigurationSource.class);
        sources.forEach((name, src) -> {
            System.out.println("[CORS-BEAN] " + name + " => " + src.getClass().getName());
            // UrlBasedCorsConfigurationSource만 상세 추적
            if (src instanceof UrlBasedCorsConfigurationSource u) {
                // 리플렉션으로 등록 맵 출력
                try {
                    var f = UrlBasedCorsConfigurationSource.class.getDeclaredField("corsConfigurations");
                    f.setAccessible(true);
                    Map<?, ?> m = (Map<?, ?>) f.get(u);
                    m.forEach((p, c) -> {
                        CorsConfiguration cfg = (CorsConfiguration) c;
                        System.out.printf("  pattern=%s, allowCred=%s, allowedOrigins=%s, patterns=%s%n",
                                p, cfg.getAllowCredentials(), cfg.getAllowedOrigins(), cfg.getAllowedOriginPatterns());
                    });
                } catch (Exception ignore) {
                    System.out.println("  Failed to inspect UrlBasedCorsConfigurationSource: " + ignore.getMessage());
                }
            }
        });

        // CorsFilter 빈도 찍기
        ctx.getBeansOfType(CorsFilter.class)
          .forEach((n,b) -> System.out.println("[CORS-FILTER] " + n + " => " + b.getClass().getName()));
        
        System.out.println("=== CORS Bean Dump End ===");
    }
}
