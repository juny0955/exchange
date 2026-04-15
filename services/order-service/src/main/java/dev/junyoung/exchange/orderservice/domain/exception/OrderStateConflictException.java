package dev.junyoung.exchange.orderservice.domain.exception;

import dev.junyoung.exchange.core.exception.domain.DomainConflictException;

public class OrderStateConflictException extends DomainConflictException {
    public OrderStateConflictException(String message) {
        super(message);
    }
}
