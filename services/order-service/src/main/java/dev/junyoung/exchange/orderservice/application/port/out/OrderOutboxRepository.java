package dev.junyoung.exchange.orderservice.application.port.out;

import dev.junyoung.exchange.orderservice.domain.model.entity.OrderOutbox;
import dev.junyoung.exchange.orderservice.domain.model.value.OutboxId;

import java.util.Optional;

public interface OrderOutboxRepository {
	void save(OrderOutbox orderOutbox);
    void updateStatus(OrderOutbox outbox);

    Optional<OrderOutbox> findById(OutboxId outboxId);
}
