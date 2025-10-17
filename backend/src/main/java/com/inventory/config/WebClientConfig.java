package com.inventory.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;

import reactor.core.publisher.Mono;
import io.netty.channel.ChannelOption;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

/**
 * WebClient 설정 클래스
 * 마이크로서비스 간 통신을 위한 WebClient 설정
 */
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        // HTTP 클라이언트 설정 (타임아웃, 연결 설정)
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofSeconds(10));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(logRequest())
                .filter(logResponse());
    }

    /**
     * 요청 로깅 필터
     */
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            System.out.println("Request: " + clientRequest.method() + " " + clientRequest.url());
            return Mono.just(clientRequest);
        });
    }

    /**
     * 응답 로깅 필터
     */
    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            System.out.println("Response: " + clientResponse.statusCode());
            return Mono.just(clientResponse);
        });
    }

    @Bean
    public WebClient tokenServiceWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl("http://localhost:8081")
                .build();
    }

    @Bean
    public WebClient registrationServiceWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl("http://localhost:8082")
                .build();
    }
}