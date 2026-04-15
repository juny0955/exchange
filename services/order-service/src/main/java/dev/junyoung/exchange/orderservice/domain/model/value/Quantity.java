package dev.junyoung.exchange.orderservice.domain.model.value;

import dev.junyoung.exchange.orderservice.domain.exception.OrderInvalidException;

import java.math.BigDecimal;

public record Quantity(
	BigDecimal value
) {
	public Quantity {
		if (value == null)
			throw new OrderInvalidException("주문 수량은 필수입니다.");

		if (value.signum() < 0)
			throw new OrderInvalidException("주문 수량은 양수이어야 합니다.");
	}

	public static Quantity zero() {
		return new Quantity(BigDecimal.ZERO);
	}

	public Quantity add(Quantity quantity) {
		return new Quantity(value.add(quantity.value));
	}

	public boolean isZero() {
		return value.signum() == 0;
	}
}
