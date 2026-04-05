package dev.junyoung.exchange.orderservice.domain.model.value;

import java.math.BigDecimal;

public record Quantity(
	BigDecimal value
) {
	public Quantity {
		if (value.signum() <= 0)
			throw new IllegalArgumentException("주문 수량은 0보다 커야합니다.");
	}
}
