package dev.junyoung.exchange.accountservice.adapter.in.grpc;

import dev.junyoung.exchange.core.exception.application.ApplicationException;
import dev.junyoung.exchange.core.exception.domain.DomainConflictException;
import dev.junyoung.exchange.core.exception.domain.DomainInvalidException;
import dev.junyoung.exchange.core.exception.infrastructure.InfrastructureException;
import io.grpc.Status;
import io.grpc.StatusException;
import org.jspecify.annotations.Nullable;
import org.springframework.grpc.server.exception.GrpcExceptionHandler;
import org.springframework.stereotype.Component;

@Component
@Deprecated
public class GrpcExceptionAdvice implements GrpcExceptionHandler {

    @Override
    public @Nullable StatusException handleException(Throwable exception) {
        return switch (exception) {
            case InfrastructureException e -> httpStatusToStatus(e.errorCode().status())
                .withDescription(e.errorCode().code() + ": " + e.getMessage())
                .asException();
            case ApplicationException e -> httpStatusToStatus(e.errorCode().status())
                .withDescription(e.errorCode().code() + ": " + e.getMessage())
                .asException();
            case DomainInvalidException e -> Status.INVALID_ARGUMENT
                .withDescription(e.getMessage())
                .asException();
            case DomainConflictException e -> Status.FAILED_PRECONDITION
                .withDescription(e.getMessage())
                .asException();
            default -> Status.INTERNAL
                .asException();
        };
    }

    private Status httpStatusToStatus(int httpStatus) {
        return switch (httpStatus) {
            case 400 -> Status.INVALID_ARGUMENT;
            case 404 -> Status.NOT_FOUND;
            case 409 -> Status.FAILED_PRECONDITION;
            default -> Status.INTERNAL;
        };
    }
}
