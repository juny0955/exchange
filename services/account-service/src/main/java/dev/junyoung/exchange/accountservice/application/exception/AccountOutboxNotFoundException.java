package dev.junyoung.exchange.accountservice.application.exception;

import dev.junyoung.exchange.core.exception.application.ApplicationException;

public class AccountOutboxNotFoundException extends ApplicationException {
    public AccountOutboxNotFoundException() {
        super(AccountErrorCode.OUTBOX_NOT_FOUND);
    }
}
