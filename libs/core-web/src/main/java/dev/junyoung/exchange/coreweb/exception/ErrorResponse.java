package dev.junyoung.exchange.coreweb.exception;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
    String errorCode,
    String message,
    String traceId,
    Instant timestamp,
    List<FieldError> fieldErrors
) {
    public record FieldError(String field, String message, Object rejectedValue) { }

    public static ErrorResponse of(String errorCode, String message, String traceId) {
        return new ErrorResponse(errorCode, message, traceId, Instant.now(), null);
    }

    public static ErrorResponse ofConflictDomain(String message, String traceId) {
        return new ErrorResponse("CONFLICT_DOMAIN", message, traceId, Instant.now(), null);
    }

    public static ErrorResponse ofValidation(List<FieldError> fieldErrors, String traceId) {
        return new ErrorResponse("INVALID_REQUEST", "Validation failed", traceId, Instant.now(), fieldErrors);
    }

    public static ErrorResponse ofServerError(String traceId) {
        return new ErrorResponse("INTERNAL_ERROR", "Unexpected error occurred", traceId, Instant.now(), null);
    }
}

