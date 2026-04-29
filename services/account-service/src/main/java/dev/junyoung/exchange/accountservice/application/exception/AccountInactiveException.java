package dev.junyoung.exchange.accountservice.application.exception;

import dev.junyoung.exchange.core.exception.application.ApplicationException;

public class AccountInactiveException extends ApplicationException {
    public AccountInactiveException() {
        super(AccountErrorCode.ACCOUNT_INACTIVE);
    }
}
