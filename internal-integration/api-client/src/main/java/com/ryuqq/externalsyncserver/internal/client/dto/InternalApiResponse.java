package com.ryuqq.externalsyncserver.internal.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record InternalApiResponse(
    @JsonProperty("requestId")
    String requestId,
    
    @JsonProperty("status")
    String status,
    
    @JsonProperty("message")
    String message,
    
    @JsonProperty("data")
    Object data,
    
    @JsonProperty("timestamp")
    LocalDateTime timestamp
) {}