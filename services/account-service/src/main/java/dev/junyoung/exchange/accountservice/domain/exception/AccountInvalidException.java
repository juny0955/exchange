package dev.junyoung.exchange.accountservice.domain.exception;

import dev.junyoung.exchange.core.exception.domain.DomainInvalidException;

public class AccountInvalidException extends DomainInvalidException {
    public AccountInvalidException(String message) {
        super(message);
    }
}
