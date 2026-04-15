package dev.junyoung.exchange.coreweb.exception;

import dev.junyoung.exchange.core.exception.application.ApplicationException;
import dev.junyoung.exchange.core.exception.infrastructure.InfrastructureException;
import dev.junyoung.exchange.core.exception.domain.DomainConflictException;
import dev.junyoung.exchange.core.exception.domain.DomainInvalidException;
import dev.junyoung.exchange.coreweb.MdcKeys;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainInvalidException.class)
    public ResponseEntity<ErrorResponse> handleInvalidDomainException(DomainInvalidException e) {
        log.error("Domain invariant violated: {}", e.getMessage(), e);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse.ofServerError(traceId()));
    }

    @ExceptionHandler(DomainConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflictDomainException(DomainConflictException e) {
        log.warn("Domain conflict: {}", e.getMessage());
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ErrorResponse.ofConflictDomain(e.getMessage(), traceId()));
    }

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponse> handleApplicationException(ApplicationException e) {
        if (e.errorCode().status() >= 500)
            log.error("[{}] Application error: {}", e.errorCode().code(), e.getMessage(), e);
        return ResponseEntity
            .status(HttpStatus.valueOf(e.errorCode().status()))
            .body(ErrorResponse.of(e.errorCode().code(), e.getMessage(), traceId()));
    }

    @ExceptionHandler(InfrastructureException.class)
    public ResponseEntity<ErrorResponse> handleInfrastructureException(InfrastructureException e) {
        // NOTE: 어댑터 throw 사이트에서 이미 로깅 중인 경우 중복 발생 — 추후 어댑터 쪽 로그 제거 필요
        log.error("[{}] Infrastructure error: {}", e.errorCode().code(), e.getMessage(), e);
        return ResponseEntity
            .status(HttpStatus.valueOf(e.errorCode().status()))
            .body(ErrorResponse.of(e.errorCode().code(), e.getMessage(), traceId()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        List<ErrorResponse.FieldError> fieldErrors = e.getBindingResult().getFieldErrors().stream()
            .map(fe -> new ErrorResponse.FieldError(fe.getField(), fe.getDefaultMessage(), fe.getRejectedValue()))
            .toList();

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.ofValidation(fieldErrors, traceId()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
        log.error("Unexpected error occurred", e);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse.ofServerError(traceId()));
    }

    private String traceId() {
        return MDC.get(MdcKeys.TRACE_ID);
    }
}
