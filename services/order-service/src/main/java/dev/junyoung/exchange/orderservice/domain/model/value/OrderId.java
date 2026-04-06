package dev.junyoung.exchange.orderservice.domain.model.value;

import java.util.UUID;

import dev.junyoung.exchange.core.exception.InvalidDomainException;

public record OrderId(
	UUID value
) {
	public OrderId {
		if (value == null)
			throw new InvalidDomainException("주문 ID는 필수 입니다.");
	}

    public static OrderId newId() {
		return new OrderId(UUID.randomUUID());
    }
}
