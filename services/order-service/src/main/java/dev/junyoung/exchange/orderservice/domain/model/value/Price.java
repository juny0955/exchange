package dev.junyoung.exchange.orderservice.domain.model.value;

import dev.junyoung.exchange.orderservice.domain.exception.OrderInvalidException;

import java.math.BigDecimal;

public record Price(
	BigDecimal value
) {
	public Price {
		if (value == null)
			throw new OrderInvalidException("주문 가격은 필수 입니다.");

		if (value.signum() <= 0)
			throw new OrderInvalidException("주문 가격은 0보다 커야합니다.");
	}
}
