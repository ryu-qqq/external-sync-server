package com.ryuqq.externalsyncserver.internal.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record InternalApiRequest(
    @JsonProperty("requestId")
    @NotBlank(message = "Request ID cannot be blank")
    String requestId,
    
    @JsonProperty("action")
    @NotBlank(message = "Action cannot be blank")
    String action,
    
    @JsonProperty("payload")
    @NotNull(message = "Payload cannot be null")
    Object payload,
    
    @JsonProperty("timestamp")
    Instant timestamp
) {}