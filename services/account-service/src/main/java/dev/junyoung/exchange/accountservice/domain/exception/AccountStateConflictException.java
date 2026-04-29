package dev.junyoung.exchange.accountservice.domain.exception;

import dev.junyoung.exchange.core.exception.domain.DomainConflictException;

public class AccountStateConflictException extends DomainConflictException {
    public AccountStateConflictException(String message) {
        super(message);
    }
}
