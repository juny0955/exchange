package dev.junyoung.exchange.orderservice.domain.model.value;

import java.math.BigDecimal;

public record QuoteQty(
	BigDecimal value
) {
	public QuoteQty {
		if (value.signum() <= 0)
			throw new IllegalArgumentException("주문 금액은 0보다 커야합니다.");
	}
}
