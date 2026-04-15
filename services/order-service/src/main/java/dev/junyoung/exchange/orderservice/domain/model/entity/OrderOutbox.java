package dev.junyoung.exchange.orderservice.domain.model.entity;

import java.time.Instant;

import dev.junyoung.exchange.orderservice.domain.model.enums.EventType;
import dev.junyoung.exchange.orderservice.domain.model.enums.OutboxStatus;
import dev.junyoung.exchange.orderservice.domain.model.value.OrderId;
import dev.junyoung.exchange.orderservice.domain.model.value.OutboxId;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderOutbox {
	private final OutboxId outboxId;
	private final OrderId orderId;
	private final EventType type;
	private final String payload;
	private OutboxStatus status;
	private int retryCount;
	private final Instant createdAt;
	private Instant publishedAt;

	public static OrderOutbox create(OrderId orderId, EventType type, String payload) {
		return new OrderOutbox(
			OutboxId.newId(),
			orderId,
			type,
			payload,
			OutboxStatus.PENDING,
			0,
			Instant.now(),
			null
		);
	}
}