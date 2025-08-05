package com.ryuqq.externalsyncserver.internal.client;

import com.ryuqq.externalsyncserver.internal.client.dto.InternalApiRequest;
import com.ryuqq.externalsyncserver.internal.client.dto.InternalApiResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class InternalApiClient {
    
    private static final Logger logger = LoggerFactory.getLogger(InternalApiClient.class);
    
    private final WebClient webClient;
    private final String baseUrl;
    
    public InternalApiClient(WebClient.Builder webClientBuilder,
                           @Value("${internal-api.base-url:http://localhost:8080}") String baseUrl) {
        this.baseUrl = baseUrl;
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .build();
    }
    
    @CircuitBreaker(name = "internal-api", fallbackMethod = "fallbackResponse")
    @Retry(name = "internal-api")
    public Mono<InternalApiResponse> sendRequest(InternalApiRequest request) {
        logger.info("Sending internal API request: {}", request.requestId());
        
        return webClient
                .post()
                .uri("/api/internal/process")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(InternalApiResponse.class)
                .timeout(Duration.ofSeconds(30))
                .doOnSuccess(response -> logger.info("Received response for request {}: {}", 
                    request.requestId(), response.status()))
                .doOnError(error -> logger.error("Error processing request {}: {}", 
                    request.requestId(), error.getMessage()));
    }
    
    @CircuitBreaker(name = "internal-api", fallbackMethod = "fallbackHealthCheck")
    public Mono<Boolean> healthCheck() {
        logger.debug("Performing health check for internal API");
        
        return webClient
                .get()
                .uri("/actuator/health")
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(10))
                .map(response -> response.contains("UP"))
                .doOnSuccess(isHealthy -> logger.debug("Health check result: {}", isHealthy))
                .doOnError(error -> logger.warn("Health check failed: {}", error.getMessage()));
    }
    
    // Fallback methods
    public Mono<InternalApiResponse> fallbackResponse(InternalApiRequest request, Exception ex) {
        logger.error("Circuit breaker activated for request {}: {}", request.requestId(), ex.getMessage());
        
        return Mono.just(new InternalApiResponse(
            request.requestId(),
            "ERROR",
            "Service temporarily unavailable: " + ex.getMessage(),
            null,
            request.timestamp()
        ));
    }
    
    public Mono<Boolean> fallbackHealthCheck(Exception ex) {
        logger.warn("Health check circuit breaker activated: {}", ex.getMessage());
        return Mono.just(false);
    }
}