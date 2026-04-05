package dev.junyoung.exchange.orderservice.domain.model.entity;

import java.time.Instant;

import dev.junyoung.exchange.core.exception.InvalidDomainException;
import dev.junyoung.exchange.orderservice.domain.model.enums.OrderStatus;
import dev.junyoung.exchange.orderservice.domain.model.enums.OrderType;
import dev.junyoung.exchange.orderservice.domain.model.enums.Side;
import dev.junyoung.exchange.orderservice.domain.model.enums.TimeInForce;
import dev.junyoung.exchange.orderservice.domain.model.value.AccountId;
import dev.junyoung.exchange.orderservice.domain.model.value.OrderId;
import dev.junyoung.exchange.orderservice.domain.model.value.Price;
import dev.junyoung.exchange.orderservice.domain.model.value.Quantity;
import dev.junyoung.exchange.orderservice.domain.model.value.QuoteQty;
import dev.junyoung.exchange.orderservice.domain.model.value.Symbol;

public record Order (
	OrderId orderId,
	AccountId accountId,
	String clientOrderId,

	Symbol symbol,
	Side side,
	OrderType orderType,
	TimeInForce tif,

	Price price,
	Quantity quantity,		// 수량 기준 (Base Asset)
	QuoteQty quoteQty,		// 금액 기준 (Quote Asset) 시장가 매수용

	Quantity cumBaseQty, 	// 누적 체결 수량 (Base Asset)
	QuoteQty cumQuoteQty, 	// 누적 체결 금액 (Quote Asset)

	OrderStatus status,

	Instant orderedAt,		// 사용자 주문 시점
	Instant createdAt,		// 시스템 접수 시점
	Instant updatedAt
) {
	public Order {
		validateCommonFields();
		switch (orderType) {
			case LIMIT -> validateLimitOrder();
			case MARKET -> validateMarketOrder();
		}
	}

	public static Order of(
		OrderId orderId,
		AccountId accountId,
		String clientOrderId,
		Symbol symbol,
		Side side,
		OrderType orderType,
		TimeInForce tif,
		Price price,
		Quantity quantity,
		QuoteQty quoteQty,
		Instant orderedAt
	) {
		Instant now = Instant.now();
		return new Order(
			orderId,
			accountId,
			clientOrderId,
			symbol,
			side,
			orderType,
			tif,
			price,
			quantity,
			quoteQty,
			Quantity.zero(),
			QuoteQty.zero(),
			OrderStatus.PENDING,
			orderedAt,
			now,
			now
		);
	}

	private void validateCommonFields() {
		if (orderId == null) throw new InvalidDomainException("주문 ID는 필수입니다.");
		if (accountId == null) throw new InvalidDomainException("계좌 ID는 필수입니다.");
		if (clientOrderId == null || clientOrderId.isBlank()) throw new InvalidDomainException("멱등 주문 ID는 필수입니다.");
		if (symbol == null) throw new InvalidDomainException("거래 심볼은 필수입니다.");
		if (side == null) throw new InvalidDomainException("주문 방향(매수/매도)은 필수입니다.");
		if (orderType == null) throw new InvalidDomainException("주문 유형은 필수입니다.");
		if (tif == null) throw new InvalidDomainException("주문 조건은 필수입니다.");
		if (orderedAt == null) throw new InvalidDomainException("주문 시점은 필수입니다.");
	}

	private void validateLimitOrder() {
		if (quoteQty != null) throw new InvalidDomainException("지정가 주문에는 금액을 지정할 수 없습니다.");
		if (price == null) throw new InvalidDomainException("지정가 주문에는 가격이 필수입니다.");
		if (quantity == null) throw new InvalidDomainException("지정가 주문에는 수량이 필수입니다.");
		if (quantity.isZero()) throw new InvalidDomainException("주문 수량은 0보다 커야합니다.");
	}

	private void validateMarketOrder() {
		switch (side) {
			case BUY -> {
				if (quantity == null && quoteQty == null) throw new InvalidDomainException("시장가 매수 주문에는 금액 또는 수량이 필요합니다.");
				if (quantity != null && quoteQty != null) throw new InvalidDomainException("시장가 매수 주문에는 금액과 수량을 동시에 지정할 수 없습니다.");
				if (quoteQty != null && quoteQty.isZero()) throw new InvalidDomainException("주문 금액은 0보다 커야합니다.");
				if (quantity != null && quantity.isZero()) throw new InvalidDomainException("주문 수량은 0보다 커야합니다.");
			}
			case SELL -> {
				if (quoteQty != null) throw new InvalidDomainException("시장가 매도 주문에는 금액을 지정할 수 없습니다.");
				if (quantity == null) throw new InvalidDomainException("시장가 매도 주문에는 수량이 필수입니다.");
				if (quantity.isZero()) throw new InvalidDomainException("주문 수량은 0보다 커야합니다.");
			}
		}
	}
}
