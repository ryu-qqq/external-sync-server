package com.ryuqq.externalsyncserver.internal.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record InternalApiResponse(
    @JsonProperty("requestId")
    String requestId,
    
    @JsonProperty("status")
    ApiStatus status,
    
    @JsonProperty("message")
    String message,
    
    @JsonProperty("data")
    Object data,
    
    @JsonProperty("timestamp")
    Instant timestamp
) {}