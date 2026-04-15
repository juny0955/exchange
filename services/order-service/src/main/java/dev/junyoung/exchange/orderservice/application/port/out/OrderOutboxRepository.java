package dev.junyoung.exchange.orderservice.application.port.out;

import dev.junyoung.exchange.orderservice.domain.model.entity.OrderOutbox;

public interface OrderOutboxRepository {
	void save(OrderOutbox orderOutbox);
}
