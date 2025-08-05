package com.ryuqq.externalsyncserver.internal.client.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("InternalApiResponse DTO 테스트")
class InternalApiResponseTest {
    
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }
    
    @Test
    @DisplayName("성공 응답 생성 테스트")
    void testSuccessResponse() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        InternalApiResponse response = new InternalApiResponse(
            "REQ-001",
            "SUCCESS",
            "Request processed successfully",
            "result data",
            now
        );
        
        // Then
        assertThat(response.requestId()).isEqualTo("REQ-001");
        assertThat(response.status()).isEqualTo("SUCCESS");
        assertThat(response.message()).isEqualTo("Request processed successfully");
        assertThat(response.data()).isEqualTo("result data");
        assertThat(response.timestamp()).isEqualTo(now);
    }
    
    @Test
    @DisplayName("에러 응답 생성 테스트")
    void testErrorResponse() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        InternalApiResponse response = new InternalApiResponse(
            "REQ-002",
            "ERROR",
            "Processing failed",
            null,
            now
        );
        
        // Then
        assertThat(response.requestId()).isEqualTo("REQ-002");
        assertThat(response.status()).isEqualTo("ERROR");
        assertThat(response.message()).isEqualTo("Processing failed");
        assertThat(response.data()).isNull();
        assertThat(response.timestamp()).isEqualTo(now);
    }
    
    @Test
    @DisplayName("JSON 직렬화/역직렬화 테스트")
    void testJsonSerialization() throws Exception {
        // Given
        LocalDateTime now = LocalDateTime.now();
        InternalApiResponse original = new InternalApiResponse(
            "REQ-001",
            "SUCCESS",
            "Request processed successfully",
            "result data",
            now
        );
        
        // When
        String json = objectMapper.writeValueAsString(original);
        InternalApiResponse deserialized = objectMapper.readValue(json, InternalApiResponse.class);
        
        // Then
        assertThat(deserialized.requestId()).isEqualTo(original.requestId());
        assertThat(deserialized.status()).isEqualTo(original.status());
        assertThat(deserialized.message()).isEqualTo(original.message());
        assertThat(deserialized.data()).isEqualTo(original.data());
        assertThat(deserialized.timestamp()).isEqualTo(original.timestamp());
    }
    
    @Test
    @DisplayName("null 값들을 포함한 응답 테스트")
    void testResponseWithNullValues() {
        // Given
        InternalApiResponse response = new InternalApiResponse(
            null,
            null,
            null,
            null,
            null
        );
        
        // Then
        assertThat(response.requestId()).isNull();
        assertThat(response.status()).isNull();
        assertThat(response.message()).isNull();
        assertThat(response.data()).isNull();
        assertThat(response.timestamp()).isNull();
    }
}