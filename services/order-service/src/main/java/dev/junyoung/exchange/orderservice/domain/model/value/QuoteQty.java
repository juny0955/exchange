package dev.junyoung.exchange.orderservice.domain.model.value;

import java.math.BigDecimal;

import dev.junyoung.exchange.core.exception.InvalidDomainException;

public record QuoteQty(
	BigDecimal value
) {
	public QuoteQty {
		if (value == null)
			throw new InvalidDomainException("주문 금액은 필수 입니다.");

		if (value.signum() <= 0)
			throw new InvalidDomainException("주문 금액은 0보다 커야합니다.");
	}
}
