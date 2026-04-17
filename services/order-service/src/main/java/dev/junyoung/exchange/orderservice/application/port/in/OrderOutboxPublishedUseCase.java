package dev.junyoung.exchange.orderservice.application.port.in;

import dev.junyoung.exchange.orderservice.domain.model.value.OutboxId;

public interface OrderOutboxPublishedUseCase {
    void orderOutboxPublished(OutboxId outboxId);
}
