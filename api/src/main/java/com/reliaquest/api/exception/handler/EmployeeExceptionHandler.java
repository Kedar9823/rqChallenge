package com.reliaquest.api.exception.handler;

import com.reliaquest.api.exception.EmployeeAPIClientException;
import com.reliaquest.api.exception.EmployeeAPIServerException;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.exception.EmployeeRateLimitException;
import com.reliaquest.api.exception.ErrorResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class EmployeeExceptionHandler {

    @ExceptionHandler({EmployeeAPIClientException.class})
    public final ResponseEntity<ErrorResponse> handleClientException(EmployeeAPIClientException exception) {

        ErrorResponse error =
                getErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), exception.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler({EmployeeAPIServerException.class})
    public final ResponseEntity<ErrorResponse> handleServerException(EmployeeAPIServerException exception) {

        ErrorResponse error =
                getErrorResponse(LocalDateTime.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(), exception.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(
            EmployeeNotFoundException exception, WebRequest request) {

        ErrorResponse error =
                getErrorResponse(LocalDateTime.now(), HttpStatus.NOT_FOUND.value(), exception.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        Map<String, List<String>> body = new HashMap<>();

        List<String> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());

        body.put("errors", errors);

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EmployeeRateLimitException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitException(EmployeeRateLimitException exception) {

        ErrorResponse error =
                getErrorResponse(LocalDateTime.now(), HttpStatus.TOO_MANY_REQUESTS.value(), exception.getMessage());

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error);
    }

    @ExceptionHandler(Throwable.class)
    public final ResponseEntity<ErrorResponse> handleGeneralExceptions(Throwable throwable) {

        ErrorResponse error =
                getErrorResponse(LocalDateTime.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(), throwable.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    private ErrorResponse getErrorResponse(LocalDateTime timestamp, int status, String error) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(timestamp);
        errorResponse.setStatus(status);
        errorResponse.setError(error);
        return errorResponse;
    }
}
