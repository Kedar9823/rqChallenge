package com.reliaquest.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class EmployeeAPIServerException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EmployeeAPIServerException(String message) {
        super(message);
    }

    public EmployeeAPIServerException(String message, Throwable cause) {
        super(message, cause);
    }
}
