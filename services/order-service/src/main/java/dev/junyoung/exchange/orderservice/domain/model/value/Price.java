package dev.junyoung.exchange.orderservice.domain.model.value;

import java.math.BigDecimal;

public record Price(
	BigDecimal value
) {
	public Price {
		if (value.signum() <= 0)
			throw new IllegalArgumentException("주문 가격은 0보다 커야합니다.");
	}
}
