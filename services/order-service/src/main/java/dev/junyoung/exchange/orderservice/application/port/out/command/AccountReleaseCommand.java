package dev.junyoung.exchange.orderservice.application.port.out.command;

import java.util.UUID;

import dev.junyoung.exchange.orderservice.domain.model.entity.Order;

public record AccountReleaseCommand(
	UUID accountId,
	UUID orderId
) {
	public static AccountReleaseCommand from(Order order) {
		return new AccountReleaseCommand(
			order.getAccountId().value(),
			order.getOrderId().value()
		);
	}
}
