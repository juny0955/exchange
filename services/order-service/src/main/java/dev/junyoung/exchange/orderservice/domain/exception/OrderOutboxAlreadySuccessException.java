package dev.junyoung.exchange.orderservice.domain.exception;

import dev.junyoung.exchange.core.exception.domain.DomainInvalidException;
import dev.junyoung.exchange.orderservice.domain.model.value.OutboxId;

public class OrderOutboxAlreadySuccessException extends DomainInvalidException {
    public OrderOutboxAlreadySuccessException(OutboxId outboxId) {
        super("해당 outbox 이벤트는 이미 성공상태입니다. outboxId: " + outboxId.value());
    }
}
