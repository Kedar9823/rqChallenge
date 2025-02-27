package com.reliaquest.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class EmployeeRegister {

    @NotBlank(message = "Name is required and cannot be blank.")
    @JsonProperty("name")
    private String name;

    @Positive(message = "Salary must be a positive number") @NotNull(message = "Salary is required and cannot be blank") @JsonProperty("salary")
    private Integer salary;

    @Min(value = 16, message = "Age must be at least 16")
    @Max(value = 75, message = "Age must be at most 75")
    @NotNull(message = "Age is required and cannot be blank") @JsonProperty("age")
    private Integer age;

    @NotBlank(message = "Title is required and cannot be blank")
    @JsonProperty("title")
    private String title;
}
