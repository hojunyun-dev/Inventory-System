package com.inventory.registration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(request -> {
                CorsConfiguration c = new CorsConfiguration();
                c.setAllowedOrigins(List.of(
                        "http://localhost:3000",
                        "http://127.0.0.1:3000"
                ));
                c.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS","PATCH"));
                c.setAllowedHeaders(List.of("*"));
                c.setExposedHeaders(List.of("Authorization","Content-Type"));
                c.setAllowCredentials(true);
                c.setMaxAge(3600L);
                return c;
            }))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/api/**").permitAll()
                .anyRequest().permitAll()
            );
        return http.build();
    }
}
