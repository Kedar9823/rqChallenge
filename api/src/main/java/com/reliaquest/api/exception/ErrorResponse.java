package com.reliaquest.api.exception;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ErrorResponse {

    private String error;
    private LocalDateTime timestamp;
    private int status;
}
