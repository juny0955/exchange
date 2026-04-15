package dev.junyoung.exchange.orderservice.application.exception;

import dev.junyoung.exchange.core.exception.application.ApplicationException;

public class OrderNotFoundException extends ApplicationException {
    public OrderNotFoundException() {
        super(OrderErrorCode.ORDER_NOT_FOUND);
    }
}
