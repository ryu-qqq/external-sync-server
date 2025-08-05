package com.ryuqq.externalsyncserver.internal.client;

import com.ryuqq.externalsyncserver.internal.client.dto.InternalApiRequest;
import com.ryuqq.externalsyncserver.internal.client.dto.InternalApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("InternalApiClient Mock 테스트")
class InternalApiClientMockTest {
    
    @Mock
    private WebClient webClient;
    
    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    
    @Mock
    private WebClient.RequestBodySpec requestBodySpec;
    
    @Mock
    private WebClient.ResponseSpec responseSpec;
    
    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    
    private InternalApiClient internalApiClient;
    
    @BeforeEach
    void setUp() {
        WebClient.Builder webClientBuilder = WebClient.builder();
        internalApiClient = new InternalApiClient(webClientBuilder, "http://localhost:8080");
    }
    
    @Test
    @DisplayName("팩토리 메서드로 생성된 클라이언트 테스트")
    void testClientCreation() {
        // Given
        WebClient.Builder webClientBuilder = WebClient.builder();
        
        // When
        InternalApiClient client = new InternalApiClient(webClientBuilder, "http://test.com");
        
        // Then
        // 클라이언트가 정상적으로 생성되는지 확인
        assert client != null;
    }
    
    @Test
    @DisplayName("Fallback 메서드 테스트")
    void testFallbackResponse() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        InternalApiRequest request = new InternalApiRequest(
            "REQ-001",
            "SYNC_PRODUCT",
            "test payload",
            now
        );
        
        Exception exception = new RuntimeException("Service unavailable");
        
        // When
        Mono<InternalApiResponse> result = internalApiClient.fallbackResponse(request, exception);
        
        // Then
        StepVerifier.create(result)
            .expectNextMatches(response -> 
                response.requestId().equals("REQ-001") &&
                response.status().equals("ERROR") &&
                response.message().contains("Service temporarily unavailable")
            )
            .verifyComplete();
    }
    
    @Test
    @DisplayName("헬스체크 Fallback 메서드 테스트")
    void testFallbackHealthCheck() {
        // Given
        Exception exception = new RuntimeException("Connection failed");
        
        // When
        Mono<Boolean> result = internalApiClient.fallbackHealthCheck(exception);
        
        // Then
        StepVerifier.create(result)
            .expectNext(false)
            .verifyComplete();
    }
}