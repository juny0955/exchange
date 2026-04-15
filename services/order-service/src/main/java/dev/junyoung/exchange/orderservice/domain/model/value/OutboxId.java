package dev.junyoung.exchange.orderservice.domain.model.value;

import dev.junyoung.exchange.orderservice.domain.exception.OrderInvalidException;

import java.util.UUID;

public record OutboxId(
	UUID value
) {
	public OutboxId {
		if (value == null)
			throw new OrderInvalidException("아웃박스 ID는 필수 입니다.");
	}

	public static OutboxId newId() {
		return new OutboxId(UUID.randomUUID());
	}
}
