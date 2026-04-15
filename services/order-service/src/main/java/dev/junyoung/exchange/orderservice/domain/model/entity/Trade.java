package dev.junyoung.exchange.orderservice.domain.model.entity;

import dev.junyoung.exchange.orderservice.domain.exception.OrderInvalidException;
import dev.junyoung.exchange.orderservice.domain.model.enums.Side;
import dev.junyoung.exchange.orderservice.domain.model.value.*;

import java.time.Instant;

public record Trade (
	TradeId tradeId,
	Symbol symbol,
	OrderId orderId,
	OrderId matchOrderId,
	Side side,
	Price price,
	Quantity quantity,
	QuoteQty quoteQty,
	Instant tradeAt,
	Instant createdAt
) {
	public Trade {
		if (tradeId == null) throw new OrderInvalidException("체결 ID는 필수입니다.");
		if (symbol == null) throw new OrderInvalidException("거래 심볼은 필수입니다.");
		if (orderId == null) throw new OrderInvalidException("주문 ID는 필수입니다.");
		if (matchOrderId == null) throw new OrderInvalidException("상대 주문 ID는 필수입니다.");
		if (side == null) throw new OrderInvalidException("주문 방향(매수/매도)은 필수입니다.");
		if (price == null) throw new OrderInvalidException("체결 가격은 필수입니다.");
		if (quantity == null) throw new OrderInvalidException("체결 수량은 필수입니다.");
		if (quantity.isZero()) throw new OrderInvalidException("체결 수량은 0보다 커야합니다.");
		if (quoteQty == null) throw new OrderInvalidException("체결 금액은 필수입니다.");
		if (quoteQty.isZero()) throw new OrderInvalidException("체결 금액은 0보다 커야합니다.");
		if (tradeAt == null) throw new OrderInvalidException("체결 시점은 필수입니다.");
	}

	public static Trade buyOf(
		TradeId tradeId,
		Symbol symbol,
		OrderId orderId,
		OrderId matchOrderId,
		Price price,
		Quantity quantity,
		QuoteQty quoteQty,
		Instant tradeAt
	) {
		return new Trade(
			tradeId,
			symbol,
			orderId,
			matchOrderId,
			Side.BUY,
			price,
			quantity,
			quoteQty,
			tradeAt,
			Instant.now()
		);
	}

	public static Trade sellOf(
		TradeId tradeId,
		Symbol symbol,
		OrderId orderId,
		OrderId matchOrderId,
		Price price,
		Quantity quantity,
		QuoteQty quoteQty,
		Instant tradeAt
	) {
		return new Trade(
			tradeId,
			symbol,
			orderId,
			matchOrderId,
			Side.SELL,
			price,
			quantity,
			quoteQty,
			tradeAt,
			Instant.now()
		);
	}
}
