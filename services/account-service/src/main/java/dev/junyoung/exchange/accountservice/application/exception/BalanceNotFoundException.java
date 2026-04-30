package dev.junyoung.exchange.accountservice.application.exception;

import dev.junyoung.exchange.core.exception.application.ApplicationException;

public class BalanceNotFoundException extends ApplicationException {
    public BalanceNotFoundException() {
        super(AccountErrorCode.BALANCE_NOT_FOUND);
    }
}
