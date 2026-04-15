package dev.junyoung.exchange.orderservice.application.port.in.command;

import java.time.Instant;

import dev.junyoung.exchange.orderservice.domain.model.value.AccountId;
import dev.junyoung.exchange.orderservice.domain.model.value.OrderId;
import dev.junyoung.exchange.orderservice.domain.model.value.Price;
import dev.junyoung.exchange.orderservice.domain.model.value.Quantity;
import dev.junyoung.exchange.orderservice.domain.model.value.QuoteQty;
import dev.junyoung.exchange.orderservice.domain.model.value.TradeId;

public record EngineMatchedCommand(
	TradeId tradeId,
	AccountId buyAccountId,
	OrderId buyOrderId,
	AccountId sellAccountId,
	OrderId sellOrderId,
	Price price,
	Quantity quantity,
	QuoteQty quoteQty,
	Instant tradeAt
) {
}
