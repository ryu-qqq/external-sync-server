package com.ryuqq.externalsyncserver.internal.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record HealthCheckResponse(
    @JsonProperty("status")
    String status,
    
    @JsonProperty("components")
    Map<String, ComponentHealth> components
) {
    public record ComponentHealth(
        @JsonProperty("status")
        String status,
        
        @JsonProperty("details")
        Map<String, Object> details
    ) {}
    
    public boolean isHealthy() {
        return "UP".equalsIgnoreCase(status);
    }
}