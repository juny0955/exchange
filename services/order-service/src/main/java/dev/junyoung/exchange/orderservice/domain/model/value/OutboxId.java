package dev.junyoung.exchange.orderservice.domain.model.value;

import java.util.UUID;

import dev.junyoung.exchange.core.exception.InvalidDomainException;

public record OutboxId(
	UUID value
) {
	public OutboxId {
		if (value == null)
			throw new InvalidDomainException("아웃박스 ID는 필수 입니다.");
	}

	public static OutboxId newId() {
		return new OutboxId(UUID.randomUUID());
	}
}
