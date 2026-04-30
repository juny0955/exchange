package dev.junyoung.exchange.accountservice.application.exception;

import dev.junyoung.exchange.core.exception.application.ApplicationException;

public class ReservationNotFoundException extends ApplicationException {
    public ReservationNotFoundException() {
        super(AccountErrorCode.RESERVATION_NOT_FOUND);
    }
}
