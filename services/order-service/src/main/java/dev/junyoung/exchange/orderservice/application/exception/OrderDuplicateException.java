package dev.junyoung.exchange.orderservice.application.exception;

import dev.junyoung.exchange.core.exception.application.ApplicationException;

public class OrderDuplicateException extends ApplicationException {
    public OrderDuplicateException() {
        super(OrderErrorCode.DUPLICATE_PLACE_ORDER);
    }
}
