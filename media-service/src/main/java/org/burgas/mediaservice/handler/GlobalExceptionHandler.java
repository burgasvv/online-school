package org.burgas.mediaservice.handler;

import org.burgas.mediaservice.dto.exception.ExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ExceptionResponse> handleThrowableException(Throwable throwable) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ExceptionResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.name())
                        .code(HttpStatus.BAD_REQUEST.value())
                        .message(throwable.getLocalizedMessage())
                        .build());
    }
}
