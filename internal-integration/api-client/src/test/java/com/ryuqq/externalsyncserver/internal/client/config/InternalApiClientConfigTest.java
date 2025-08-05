package com.ryuqq.externalsyncserver.internal.client.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.RetryConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("InternalApiClientConfig 설정 테스트")
class InternalApiClientConfigTest {
    
    private final InternalApiClientConfig config = new InternalApiClientConfig();
    
    @Test
    @DisplayName("WebClient.Builder 빈 생성 테스트")
    void testWebClientBuilderBean() {
        // When
        WebClient.Builder builder = config.webClientBuilder();
        
        // Then
        assertThat(builder).isNotNull();
        
        WebClient webClient = builder.build();
        assertThat(webClient).isNotNull();
    }
    
    @Test
    @DisplayName("CircuitBreaker 설정 테스트")
    void testCircuitBreakerConfig() {
        // When
        CircuitBreakerConfig circuitBreakerConfig = config.circuitBreakerConfig();
        
        // Then
        assertThat(circuitBreakerConfig).isNotNull();
        assertThat(circuitBreakerConfig.getFailureRateThreshold()).isEqualTo(50.0f);
        assertThat(circuitBreakerConfig.getSlidingWindowSize()).isEqualTo(10);
        assertThat(circuitBreakerConfig.getMinimumNumberOfCalls()).isEqualTo(5);
    }
    
    @Test
    @DisplayName("Retry 설정 테스트")
    void testRetryConfig() {
        // When
        RetryConfig retryConfig = config.retryConfig();
        
        // Then
        assertThat(retryConfig).isNotNull();
        assertThat(retryConfig.getMaxAttempts()).isEqualTo(3);
        // Resilience4j 2.1.0에서는 내부 구현 방식이 변경됨
    }
}