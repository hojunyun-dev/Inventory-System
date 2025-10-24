package com.inventory.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/debug")
public class CorsDebugController {

    @Autowired
    private ApplicationContext applicationContext;

    @GetMapping("/cors-config")
    public String getCorsConfig() {
        StringBuilder result = new StringBuilder();
        
        try {
            // CORS ConfigurationSource Bean들 찾기
            String[] corsBeanNames = applicationContext.getBeanNamesForType(CorsConfigurationSource.class);
            result.append("=== CORS ConfigurationSource Beans ===\n");
            for (String beanName : corsBeanNames) {
                result.append("Bean Name: ").append(beanName).append("\n");
                CorsConfigurationSource bean = applicationContext.getBean(beanName, CorsConfigurationSource.class);
                result.append("Bean Type: ").append(bean.getClass().getName()).append("\n");
                
                // 실제 CORS 설정 확인
                CorsConfiguration config = bean.getCorsConfiguration(null);
                if (config != null) {
                    result.append("Allowed Origins: ").append(config.getAllowedOrigins()).append("\n");
                    result.append("Allowed Methods: ").append(config.getAllowedMethods()).append("\n");
                    result.append("Allowed Headers: ").append(config.getAllowedHeaders()).append("\n");
                    result.append("Allow Credentials: ").append(config.getAllowCredentials()).append("\n");
                    result.append("Exposed Headers: ").append(config.getExposedHeaders()).append("\n");
                }
                result.append("---\n");
            }
        } catch (Exception e) {
            result.append("Error: ").append(e.getMessage()).append("\n");
        }
        
        return result.toString();
    }
}

