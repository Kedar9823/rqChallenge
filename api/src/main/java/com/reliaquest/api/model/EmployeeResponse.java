package com.reliaquest.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class EmployeeResponse<T> {

    @JsonProperty("data")
    private T data;

    @JsonProperty("status")
    private Status status;

    @JsonProperty("error")
    private String error;
}
