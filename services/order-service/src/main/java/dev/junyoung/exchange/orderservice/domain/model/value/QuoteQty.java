package dev.junyoung.exchange.orderservice.domain.model.value;

import java.math.BigDecimal;
import java.util.Objects;

public record QuoteQty(
	BigDecimal value
) {
	public QuoteQty {
		Objects.requireNonNull(value, "주문 금액은 필수 입니다.");

		if (value.signum() <= 0)
			throw new IllegalArgumentException("주문 금액은 0보다 커야합니다.");
	}
}
