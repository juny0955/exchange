package dev.junyoung.exchange.orderservice.domain.model.value;

import dev.junyoung.exchange.orderservice.domain.exception.OrderInvalidException;

import java.util.UUID;

public record AccountId(
	UUID value
) {
	public AccountId {
		if (value == null)
			throw new OrderInvalidException("계좌 ID는 필수 입니다.");
	}
}
