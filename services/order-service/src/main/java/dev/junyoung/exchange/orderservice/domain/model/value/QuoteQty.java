package dev.junyoung.exchange.orderservice.domain.model.value;

import dev.junyoung.exchange.orderservice.domain.exception.OrderInvalidException;

import java.math.BigDecimal;

public record QuoteQty(
	BigDecimal value
) {
	public QuoteQty {
		if (value == null)
			throw new OrderInvalidException("주문 금액은 필수입니다.");

		if (value.signum() < 0)
			throw new OrderInvalidException("주문 금액은 양수이어야 합니다.");
	}

	public static QuoteQty zero() {
		return new QuoteQty(BigDecimal.ZERO);
	}

	public QuoteQty add(QuoteQty quoteQty) {
		return new QuoteQty(value.add(quoteQty.value));
	}

	public boolean isZero() {
		return value.signum() == 0;
	}
}
