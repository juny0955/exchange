package dev.junyoung.exchange.orderservice.domain.exception;

import dev.junyoung.exchange.core.exception.domain.DomainInvalidException;

public class OrderInvalidException extends DomainInvalidException {
    public OrderInvalidException(String message) {
        super(message);
    }
}
