package dev.junyoung.exchange.orderservice.domain.model.entity;

import java.time.Instant;

import dev.junyoung.exchange.core.exception.InvalidDomainException;
import dev.junyoung.exchange.orderservice.domain.model.enums.Side;
import dev.junyoung.exchange.orderservice.domain.model.value.OrderId;
import dev.junyoung.exchange.orderservice.domain.model.value.Price;
import dev.junyoung.exchange.orderservice.domain.model.value.Quantity;
import dev.junyoung.exchange.orderservice.domain.model.value.Symbol;
import dev.junyoung.exchange.orderservice.domain.model.value.TradeId;

public record Trade (
	TradeId tradeId,
	Symbol symbol,
	OrderId orderId,
	OrderId matchOrderId,
	Side side,
	Price price,
	Quantity quantity,
	Instant tradeAt,
	Instant createdAt
) {
	public Trade {
		if (tradeId == null) throw new InvalidDomainException("체결 ID는 필수입니다.");
		if (symbol == null) throw new InvalidDomainException("거래 심볼은 필수입니다.");
		if (orderId == null) throw new InvalidDomainException("주문 ID는 필수입니다.");
		if (matchOrderId == null) throw new InvalidDomainException("상대 주문 ID는 필수입니다.");
		if (side == null) throw new InvalidDomainException("주문 방향(매수/매도)은 필수입니다.");
		if (price == null) throw new InvalidDomainException("체결 가격은 필수입니다.");
		if (quantity == null) throw new InvalidDomainException("체결 수량은 필수입니다.");
		if (quantity.isZero()) throw new InvalidDomainException("체결 수량은 0보다 커야합니다.");
		if (tradeAt == null) throw new InvalidDomainException("체결 시점은 필수입니다.");
	}
}
