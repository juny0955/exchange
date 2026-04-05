package dev.junyoung.exchange.orderservice.domain.model.value;

import java.math.BigDecimal;

import dev.junyoung.exchange.core.exception.InvalidDomainException;

public record Quantity(
	BigDecimal value
) {
	public Quantity {
		if (value == null)
			throw new InvalidDomainException("주문 수량은 필수 입니다.");

		if (value.signum() < 0)
			throw new InvalidDomainException("주문 수량은 양수이어야 합니다.");
	}

	public static Quantity zero() {
		return new Quantity(BigDecimal.ZERO);
	}

	public boolean isZero() {
		return value.signum() == 0;
	}
}
