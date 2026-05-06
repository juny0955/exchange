package dev.junyoung.exchange.accountservice.application.port.out;

import java.util.Optional;

import dev.junyoung.exchange.accountservice.domain.model.entity.AccountOutbox;
import dev.junyoung.exchange.accountservice.domain.model.value.OutboxId;

public interface AccountOutboxRepository {
    void save(AccountOutbox outbox);
    Optional<AccountOutbox> findById(OutboxId outboxId);
    void updateStatus(AccountOutbox outbox);
}
