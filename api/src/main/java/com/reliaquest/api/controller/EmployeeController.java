package com.reliaquest.api.controller;

import com.reliaquest.api.exception.EmployeeAPIClientException;
import com.reliaquest.api.exception.EmployeeAPIServerException;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.exception.EmployeeRateLimitException;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeRegister;
import com.reliaquest.api.service.EmployeeService;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Kedar10
 *
 */
@RestController
@RequestMapping("api/v2/employees")
public class EmployeeController implements IEmployeeController<Employee, EmployeeRegister> {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(EmployeeController.class);

    @GetMapping()
    public ResponseEntity<List<Employee>> getAllEmployees() {

        LOGGER.info("Inside getAllEmployees method : EmployeeController");

        List<Employee> employeeInfos = new ArrayList<>();

        try {

            employeeInfos = employeeService.getEmployeeInfo();

            return new ResponseEntity<List<Employee>>(employeeInfos, HttpStatus.OK);

        } catch (Exception exception) {

            LOGGER.info("Exception occurred while fetching employee details");

            if (exception.getCause() instanceof EmployeeAPIServerException) {
                throw (EmployeeAPIServerException) exception.getCause();
            } else if (exception.getCause() instanceof EmployeeAPIClientException) {
                throw (EmployeeAPIClientException) exception.getCause();
            } else if (exception.getCause() instanceof EmployeeNotFoundException) {
                throw (EmployeeNotFoundException) exception.getCause();
            } else if (exception.getCause() instanceof EmployeeRateLimitException) {
                throw (EmployeeRateLimitException) exception.getCause();
            } else {
                throw exception;
            }
        }
    }

    @GetMapping("/search/{searchString}")
    public ResponseEntity<List<Employee>> getEmployeesByNameSearch(
            @PathVariable(value = "searchString", required = false) String searchString) {

        LOGGER.info("Inside getEmployeesByNameSearch method : EmployeeController");

        List<Employee> employeeInfos = new ArrayList<>();

        try {
            employeeInfos = employeeService.getEmployeesByNameSearch(searchString);

            LOGGER.info("Employee names which contains {} : {}", searchString, employeeInfos);

            return new ResponseEntity<List<Employee>>(employeeInfos, HttpStatus.OK);
        } catch (Exception exception) {

            LOGGER.info(
                    "Exception occurred while fetching employee details by string input provided: {}", searchString);

            if (exception.getCause() instanceof EmployeeAPIServerException) {
                throw (EmployeeAPIServerException) exception.getCause();
            } else if (exception.getCause() instanceof EmployeeAPIClientException) {
                throw (EmployeeAPIClientException) exception.getCause();
            } else if (exception.getCause() instanceof EmployeeNotFoundException) {
                throw (EmployeeNotFoundException) exception.getCause();
            } else if (exception.getCause() instanceof EmployeeRateLimitException) {
                throw (EmployeeRateLimitException) exception.getCause();
            } else {
                throw exception;
            }
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable(value = "id") String id) {

        LOGGER.info("Inside getEmployeeById method : EmployeeController");

        Employee employeeInfoById;

        try {

            employeeInfoById = employeeService.getEmployeeInfoById(id).block();

            return new ResponseEntity<Employee>(employeeInfoById, HttpStatus.OK);

        } catch (Exception exception) {

            LOGGER.info("Exception occurred while fetching employees by id: {}", id);

            if (exception.getCause() instanceof EmployeeAPIServerException) {
                throw (EmployeeAPIServerException) exception.getCause();
            } else if (exception.getCause() instanceof EmployeeAPIClientException) {
                throw (EmployeeAPIClientException) exception.getCause();
            } else if (exception.getCause() instanceof EmployeeNotFoundException) {
                throw (EmployeeNotFoundException) exception.getCause();
            } else if (exception.getCause() instanceof EmployeeRateLimitException) {
                throw (EmployeeRateLimitException) exception.getCause();
            } else {
                throw exception;
            }
        }
    }

    @GetMapping("/highestSalary")
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {

        LOGGER.info("Inside getHighestSalaryOfEmployees method : EmployeeController");

        try {

            Integer highestSalary = employeeService.getHighestSalaryOfEmployees();

            LOGGER.info("Highest salary of amongst all employees : {}", highestSalary);

            return new ResponseEntity<Integer>(highestSalary, HttpStatus.OK);

        } catch (Exception exception) {

            LOGGER.info("Exception occurred while fetching employees highest salary");

            if (exception.getCause() instanceof EmployeeAPIServerException) {
                throw (EmployeeAPIServerException) exception.getCause();
            } else if (exception.getCause() instanceof EmployeeAPIClientException) {
                throw (EmployeeAPIClientException) exception.getCause();
            } else if (exception.getCause() instanceof EmployeeNotFoundException) {
                throw (EmployeeNotFoundException) exception.getCause();
            } else if (exception.getCause() instanceof EmployeeRateLimitException) {
                throw (EmployeeRateLimitException) exception.getCause();
            } else {
                throw exception;
            }
        }
    }

    @GetMapping("/topTenHighestEarningEmployeeNames")
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {

        LOGGER.info("Inside getTopTenHighestEarningEmployeeNames method : EmployeeController");

        try {

            List<String> top10EmployeeNames = employeeService.getTopTenHighestEarningEmployeeNames();

            LOGGER.info("Top Ten highest salary employees names : {}", top10EmployeeNames);

            return new ResponseEntity<List<String>>(top10EmployeeNames, HttpStatus.OK);

        } catch (Exception exception) {

            LOGGER.info("Exception occurred while fetching top ten highest employees highest salary");

            if (exception.getCause() instanceof EmployeeAPIServerException) {
                throw (EmployeeAPIServerException) exception.getCause();
            } else if (exception.getCause() instanceof EmployeeAPIClientException) {
                throw (EmployeeAPIClientException) exception.getCause();
            } else if (exception.getCause() instanceof EmployeeNotFoundException) {
                throw (EmployeeNotFoundException) exception.getCause();
            } else if (exception.getCause() instanceof EmployeeRateLimitException) {
                throw (EmployeeRateLimitException) exception.getCause();
            } else {
                throw exception;
            }
        }
    }

    @PostMapping()
    public ResponseEntity<Employee> createEmployee(@Valid @RequestBody EmployeeRegister employeeInput) {

        LOGGER.info("Inside createEmployee method : EmployeeController");

        try {

            Employee employeeInfoById =
                    employeeService.createEmployee(employeeInput).block();

            return new ResponseEntity<Employee>(employeeInfoById, HttpStatus.OK);

        } catch (Exception exception) {

            LOGGER.info("Exception occurred while creating employees");

            if (exception.getCause() instanceof EmployeeAPIServerException) {
                throw (EmployeeAPIServerException) exception.getCause();
            } else if (exception.getCause() instanceof EmployeeAPIClientException) {
                throw (EmployeeAPIClientException) exception.getCause();
            } else if (exception.getCause() instanceof EmployeeNotFoundException) {
                throw (EmployeeNotFoundException) exception.getCause();
            } else if (exception.getCause() instanceof EmployeeRateLimitException) {
                throw (EmployeeRateLimitException) exception.getCause();
            } else {
                throw exception;
            }
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEmployeeById(@PathVariable(value = "id") String id) {

        LOGGER.info("Inside deleteEmployeeById method : EmployeeController");

        try {

            Employee employeeInfoById = employeeService.getEmployeeInfoById(id).block();

            Boolean result = employeeService.deleteEmployee(employeeInfoById).block();

            if (!result) {
                throw new EmployeeAPIServerException("Exception occurred while deleting employee by id");
            }

            return new ResponseEntity<String>(employeeInfoById.getEmployeeName(), HttpStatus.OK);

        } catch (Exception exception) {

            LOGGER.info("Exception occurred while fetching employees by id: {}", id);

            if (exception.getCause() instanceof EmployeeAPIServerException) {
                throw (EmployeeAPIServerException) exception.getCause();
            } else if (exception.getCause() instanceof EmployeeAPIClientException) {
                throw (EmployeeAPIClientException) exception.getCause();
            } else if (exception.getCause() instanceof EmployeeNotFoundException) {
                throw (EmployeeNotFoundException) exception.getCause();
            } else if (exception.getCause() instanceof EmployeeRateLimitException) {
                throw (EmployeeRateLimitException) exception.getCause();
            } else {
                throw exception;
            }
        }
    }
}
