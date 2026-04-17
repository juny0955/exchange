package dev.junyoung.exchange.orderservice.application.exception;

import dev.junyoung.exchange.core.exception.application.ApplicationException;

public class OrderOutboxNotFoundException extends ApplicationException {
    public OrderOutboxNotFoundException() {
        super(OrderErrorCode.ORDER_OUTBOX_NOT_FOUND);
    }
}
