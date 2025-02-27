package com.reliaquest.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class EmployeeDeletion {

    @JsonProperty
    private String name;
}
