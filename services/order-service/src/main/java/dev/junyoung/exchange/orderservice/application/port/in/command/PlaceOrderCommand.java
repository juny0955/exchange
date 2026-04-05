package dev.junyoung.exchange.orderservice.application.port.in.command;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import dev.junyoung.exchange.orderservice.domain.model.enums.OrderType;
import dev.junyoung.exchange.orderservice.domain.model.enums.Side;
import dev.junyoung.exchange.orderservice.domain.model.enums.TimeInForce;
import dev.junyoung.exchange.orderservice.domain.model.value.AccountId;
import dev.junyoung.exchange.orderservice.domain.model.value.Price;
import dev.junyoung.exchange.orderservice.domain.model.value.Quantity;
import dev.junyoung.exchange.orderservice.domain.model.value.QuoteQty;
import dev.junyoung.exchange.orderservice.domain.model.value.Symbol;

public record PlaceOrderCommand(
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
	public PlaceOrderCommand(
		UUID accountId,
		String clientOrderId,
		String baseAsset,
		String quoteAsset,
		String side,
		String orderType,
		String tif,
		BigDecimal price,
		BigDecimal quantity,
		BigDecimal quoteQty,
		Instant orderedAt
	) {
		this(
			new AccountId(accountId),
			clientOrderId,
			new Symbol(baseAsset, quoteAsset),
			Side.valueOf(side),
			OrderType.valueOf(orderType),
			TimeInForce.valueOf(tif),
			price == null ? null : new Price(price),
			quantity == null ? null : new Quantity(quantity),
			quoteQty == null ? null : new QuoteQty(quoteQty),
			orderedAt
		);
	}
}
