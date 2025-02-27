package com.reliaquest.api.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

public enum Status {
    HANDLED("Successfully processed request."),
    ERROR("Failed to process request.");

    @JsonValue
    @Getter
    private final String value;

    Status(String value) {
        this.value = value;
    }
}
