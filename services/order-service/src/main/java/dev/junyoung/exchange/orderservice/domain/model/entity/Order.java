package dev.junyoung.exchange.orderservice.domain.model.entity;

import java.time.Instant;

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
}
