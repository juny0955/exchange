package dev.junyoung.exchange.orderservice.application.port.in;

import java.time.Instant;

import dev.junyoung.exchange.orderservice.domain.model.value.OutboxId;

public interface CompleteOrderOutboxUseCase {
    void completeOrderOutbox(OutboxId outboxId, Instant publishedAt);
}
