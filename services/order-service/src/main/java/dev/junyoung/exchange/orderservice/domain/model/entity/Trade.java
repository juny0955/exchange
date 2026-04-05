package dev.junyoung.exchange.orderservice.domain.model.entity;

import java.time.Instant;

import dev.junyoung.exchange.orderservice.domain.model.value.OrderId;
import dev.junyoung.exchange.orderservice.domain.model.value.Price;
import dev.junyoung.exchange.orderservice.domain.model.value.Quantity;
import dev.junyoung.exchange.orderservice.domain.model.value.Symbol;
import dev.junyoung.exchange.orderservice.domain.model.value.TradeId;

public record Trade (
	TradeId tradeId,
	Symbol symbol,
	OrderId buyOrderId,
	OrderId sellOrderId,
	Price price,
	Quantity quantity,
	Instant createdAt
) {
}
