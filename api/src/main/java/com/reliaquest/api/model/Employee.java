package com.reliaquest.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Employee {

    @JsonProperty("id")
    private UUID uuid;

    @JsonProperty("employee_name")
    private String employeeName;

    @JsonProperty("employee_salary")
    private Integer employeeSalary;

    @JsonProperty("employee_age")
    private Integer employeeAge;

    @JsonProperty("employee_title")
    private String employeeTitle;

    @JsonProperty("employee_email")
    private String employeeEmail;
}
