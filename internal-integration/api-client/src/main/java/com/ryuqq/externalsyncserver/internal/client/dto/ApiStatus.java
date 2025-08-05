package com.ryuqq.externalsyncserver.internal.client.dto;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ApiStatus {
    SUCCESS("SUCCESS"),
    ERROR("ERROR"),
    PENDING("PENDING"),
    TIMEOUT("TIMEOUT"),
    RETRY("RETRY");
    
    private final String value;
    
    ApiStatus(String value) {
        this.value = value;
    }
    
    @JsonValue
    public String getValue() {
        return value;
    }
    
    public static ApiStatus fromValue(String value) {
        for (ApiStatus status : ApiStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + value);
    }
}