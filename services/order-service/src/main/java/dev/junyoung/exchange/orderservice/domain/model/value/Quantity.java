package dev.junyoung.exchange.orderservice.domain.model.value;

import java.math.BigDecimal;
import java.util.Objects;

public record Quantity(
	BigDecimal value
) {
	public Quantity {
		Objects.requireNonNull(value, "주문 수량은 필수 입니다.");

		if (value.signum() <= 0)
			throw new IllegalArgumentException("주문 수량은 0보다 커야합니다.");
	}
}
