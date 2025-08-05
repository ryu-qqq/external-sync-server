package com.ryuqq.externalsyncserver.internal.client.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("InternalApiRequest DTO 테스트")
class InternalApiRequestTest {
    
    private Validator validator;
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }
    
    @Test
    @DisplayName("유효한 요청 생성 테스트")
    void testValidRequest() {
        // Given
        InternalApiRequest request = new InternalApiRequest(
            "REQ-001",
            "SYNC_PRODUCT",
            "test payload",
            Instant.now()
        );
        
        // When
        Set<ConstraintViolation<InternalApiRequest>> violations = validator.validate(request);
        
        // Then
        assertThat(violations).isEmpty();
        assertThat(request.requestId()).isEqualTo("REQ-001");
        assertThat(request.action()).isEqualTo("SYNC_PRODUCT");
        assertThat(request.payload()).isEqualTo("test payload");
        assertThat(request.timestamp()).isNotNull();
    }
    
    @Test
    @DisplayName("requestId가 null인 경우 검증 실패")
    void testNullRequestId() {
        // Given
        InternalApiRequest request = new InternalApiRequest(
            null,
            "SYNC_PRODUCT",
            "test payload",
            Instant.now()
        );
        
        // When
        Set<ConstraintViolation<InternalApiRequest>> violations = validator.validate(request);
        
        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Request ID cannot be blank");
    }
    
    @Test
    @DisplayName("requestId가 빈 문자열인 경우 검증 실패")
    void testBlankRequestId() {
        // Given
        InternalApiRequest request = new InternalApiRequest(
            "",
            "SYNC_PRODUCT",
            "test payload",
            Instant.now()
        );
        
        // When
        Set<ConstraintViolation<InternalApiRequest>> violations = validator.validate(request);
        
        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Request ID cannot be blank");
    }
    
    @Test
    @DisplayName("action이 null인 경우 검증 실패")
    void testNullAction() {
        // Given
        InternalApiRequest request = new InternalApiRequest(
            "REQ-001",
            null,
            "test payload",
            Instant.now()
        );
        
        // When
        Set<ConstraintViolation<InternalApiRequest>> violations = validator.validate(request);
        
        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Action cannot be blank");
    }
    
    @Test
    @DisplayName("payload가 null인 경우 검증 실패")
    void testNullPayload() {
        // Given
        InternalApiRequest request = new InternalApiRequest(
            "REQ-001",
            "SYNC_PRODUCT",
            null,
            Instant.now()
        );
        
        // When
        Set<ConstraintViolation<InternalApiRequest>> violations = validator.validate(request);
        
        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Payload cannot be null");
    }
    
    @Test
    @DisplayName("JSON 직렬화/역직렬화 테스트")
    void testJsonSerialization() throws Exception {
        // Given
        Instant now = Instant.now();
        InternalApiRequest original = new InternalApiRequest(
            "REQ-001",
            "SYNC_PRODUCT",
            "test payload",
            now
        );
        
        // When
        String json = objectMapper.writeValueAsString(original);
        InternalApiRequest deserialized = objectMapper.readValue(json, InternalApiRequest.class);
        
        // Then
        assertThat(deserialized.requestId()).isEqualTo(original.requestId());
        assertThat(deserialized.action()).isEqualTo(original.action());
        assertThat(deserialized.payload()).isEqualTo(original.payload());
        assertThat(deserialized.timestamp()).isEqualTo(original.timestamp());
    }
}