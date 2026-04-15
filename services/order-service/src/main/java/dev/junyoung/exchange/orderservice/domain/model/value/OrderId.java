package dev.junyoung.exchange.orderservice.domain.model.value;

import dev.junyoung.exchange.orderservice.domain.exception.OrderInvalidException;

import java.util.UUID;

public record OrderId(
	UUID value
) {
	public OrderId {
		if (value == null)
			throw new OrderInvalidException("주문 ID는 필수 입니다.");
	}

    public static OrderId newId() {
		return new OrderId(UUID.randomUUID());
    }
}
