package dev.junyoung.exchange.accountservice.domain.exception;

import dev.junyoung.exchange.core.exception.domain.DomainConflictException;

public class InsufficientBalanceException extends DomainConflictException {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}
