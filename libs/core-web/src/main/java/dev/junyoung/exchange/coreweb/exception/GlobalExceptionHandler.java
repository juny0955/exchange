package dev.junyoung.exchange.coreweb.exception;

import dev.junyoung.exchange.core.exception.CoreException;
import dev.junyoung.exchange.coreweb.web.TraceIdFilter;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CoreException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(CoreException e) {
        return ResponseEntity
            .status(HttpStatus.valueOf(e.errorCode().status()))
            .body(ErrorResponse.of(e.errorCode().code(), e.getMessage(), traceId()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        List<ErrorResponse.FieldError> fieldErrors = e.getBindingResult().getFieldErrors().stream()
            .map(fe -> new ErrorResponse.FieldError(
                fe.getField(), fe.getDefaultMessage(), null))
            .toList();

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.ofValidation("INVALID_REQUEST", "Validation failed", traceId(), fieldErrors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse.of("INTERNAL_ERROR", "Unexpected error occurred", traceId()));
    }

    private String traceId() {
        return MDC.get(TraceIdFilter.traceIdKey());
    }
}
