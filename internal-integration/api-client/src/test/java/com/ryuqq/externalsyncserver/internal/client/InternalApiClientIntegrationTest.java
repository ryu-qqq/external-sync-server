package com.ryuqq.externalsyncserver.internal.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ryuqq.externalsyncserver.internal.client.dto.ApiStatus;
import com.ryuqq.externalsyncserver.internal.client.dto.HealthCheckResponse;
import com.ryuqq.externalsyncserver.internal.client.dto.InternalApiRequest;
import com.ryuqq.externalsyncserver.internal.client.dto.InternalApiResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("InternalApiClient 통합 테스트")
class InternalApiClientIntegrationTest {
    
    private MockWebServer mockWebServer;
    private InternalApiClient internalApiClient;
    private ObjectMapper objectMapper;
    
    {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }
    
    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        
        String baseUrl = mockWebServer.url("/").toString();
        WebClient.Builder webClientBuilder = WebClient.builder();
        internalApiClient = new InternalApiClient(webClientBuilder, baseUrl);
    }
    
    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }
    
    @Test
    @DisplayName("성공적인 API 요청 테스트")
    void testSuccessfulApiRequest() throws JsonProcessingException, InterruptedException {
        // Given
        Instant now = Instant.now();
        InternalApiRequest request = new InternalApiRequest(
            "REQ-001",
            "SYNC_PRODUCT",
            Map.of("productId", "12345", "name", "Test Product"),
            now
        );
        
        InternalApiResponse expectedResponse = new InternalApiResponse(
            "REQ-001",
            ApiStatus.SUCCESS,
            "Request processed successfully",
            Map.of("processedAt", now.toString()),
            now
        );
        
        mockWebServer.enqueue(new MockResponse()
            .setBody(objectMapper.writeValueAsString(expectedResponse))
            .addHeader("Content-Type", "application/json"));
        
        // When
        Mono<InternalApiResponse> result = internalApiClient.sendRequest(request);
        
        // Then
        StepVerifier.create(result)
            .expectNextMatches(response -> 
                response.requestId().equals("REQ-001") &&
                response.status().equals(ApiStatus.SUCCESS) &&
                response.message().equals("Request processed successfully")
            )
            .verifyComplete();
        
        // Verify the request was made correctly
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        assertThat(recordedRequest.getPath()).isEqualTo(ApiConstants.PROCESS_ENDPOINT);
        assertThat(recordedRequest.getHeader("Content-Type")).contains("application/json");
    }
    
    @Test
    @DisplayName("API 요청 실패 시 타임아웃 테스트")
    void testApiRequestTimeout() {
        // Given
        Instant now = Instant.now();
        InternalApiRequest request = new InternalApiRequest(
            "REQ-002",
            "SYNC_PRODUCT",
            Map.of("productId", "12345"),
            now
        );
        
        // Mock server doesn't respond (timeout scenario)
        mockWebServer.enqueue(new MockResponse().setBodyDelay(35, java.util.concurrent.TimeUnit.SECONDS));
        
        // When
        Mono<InternalApiResponse> result = internalApiClient.sendRequest(request);
        
        // Then
        StepVerifier.create(result)
            .expectError()
            .verify(Duration.ofSeconds(35));
    }
    
    @Test
    @DisplayName("헬스체크 성공 테스트")
    void testSuccessfulHealthCheck() throws JsonProcessingException {
        // Given
        HealthCheckResponse healthResponse = new HealthCheckResponse(
            "UP",
            Map.of("database", new HealthCheckResponse.ComponentHealth("UP", Map.of()))
        );
        
        mockWebServer.enqueue(new MockResponse()
            .setBody(objectMapper.writeValueAsString(healthResponse))
            .addHeader("Content-Type", "application/json"));
        
        // When
        Mono<Boolean> result = internalApiClient.healthCheck();
        
        // Then
        StepVerifier.create(result)
            .expectNext(true)
            .verifyComplete();
    }
    
    @Test
    @DisplayName("헬스체크 실패 테스트")
    void testFailedHealthCheck() throws JsonProcessingException {
        // Given
        HealthCheckResponse healthResponse = new HealthCheckResponse(
            "DOWN",
            Map.of("database", new HealthCheckResponse.ComponentHealth("DOWN", 
                Map.of("error", "Connection refused")))
        );
        
        mockWebServer.enqueue(new MockResponse()
            .setBody(objectMapper.writeValueAsString(healthResponse))
            .addHeader("Content-Type", "application/json"));
        
        // When
        Mono<Boolean> result = internalApiClient.healthCheck();
        
        // Then
        StepVerifier.create(result)
            .expectNext(false)
            .verifyComplete();
    }
    
    @Test
    @DisplayName("API 에러 응답 처리 테스트")
    void testApiErrorResponse() throws JsonProcessingException {
        // Given
        Instant now = Instant.now();
        InternalApiRequest request = new InternalApiRequest(
            "REQ-003",
            "INVALID_ACTION",
            Map.of("productId", "invalid"),
            now
        );
        
        InternalApiResponse errorResponse = new InternalApiResponse(
            "REQ-003",
            ApiStatus.ERROR,
            "Invalid action specified",
            null,
            now
        );
        
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(400)
            .setBody(objectMapper.writeValueAsString(errorResponse))
            .addHeader("Content-Type", "application/json"));
        
        // When
        Mono<InternalApiResponse> result = internalApiClient.sendRequest(request);
        
        // Then
        StepVerifier.create(result)
            .expectError()
            .verify();
    }
}