package com.reliaquest.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class EmployeeAPIClientException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EmployeeAPIClientException(String message) {
        super(message);
    }

    public EmployeeAPIClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
