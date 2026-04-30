package dev.junyoung.exchange.accountservice.application.exception;

import dev.junyoung.exchange.core.exception.application.ApplicationException;

public class AccountNotFoundException extends ApplicationException {
    public AccountNotFoundException() {
        super(AccountErrorCode.ACCOUNT_NOT_FOUND);
    }
}
