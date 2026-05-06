package dev.junyoung.exchange.accountservice.application.port.in;

import dev.junyoung.exchange.accountservice.domain.model.value.OutboxId;

import java.time.Instant;

public interface CompleteAccountOutboxUseCase {
    void complete(OutboxId outboxId, Instant publishedAt);
}
