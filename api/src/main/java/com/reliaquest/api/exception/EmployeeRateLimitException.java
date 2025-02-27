package com.reliaquest.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.TOO_MANY_REQUESTS)
public class EmployeeRateLimitException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EmployeeRateLimitException() {
        super();
    }

    public EmployeeRateLimitException(String message) {
        super(message);
    }

    public EmployeeRateLimitException(String message, Throwable cause) {
        super(message, cause);
    }
}
