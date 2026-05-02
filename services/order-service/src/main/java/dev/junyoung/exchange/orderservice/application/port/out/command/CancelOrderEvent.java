package dev.junyoung.exchange.orderservice.application.port.out.command;

import java.util.UUID;

import dev.junyoung.exchange.orderservice.domain.model.entity.Order;

public record CancelOrderEvent(
    UUID orderId,
    UUID accountId,
    OrderSymbolEvent symbol
) {
	public static CancelOrderEvent from(Order order) {
		return new CancelOrderEvent(
			order.getOrderId().value(),
			order.getAccountId().value(),
			OrderSymbolEvent.from(order.getSymbol())
		);
	}
}
