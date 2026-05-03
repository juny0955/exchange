package dev.junyoung.exchange.orderservice.application.port.out.command;

import java.math.BigDecimal;
import java.util.UUID;

import dev.junyoung.exchange.orderservice.domain.model.entity.Order;
import dev.junyoung.exchange.orderservice.domain.service.dto.AssetReserveResult;

public record ReserveOrderEvent(
	UUID orderId,
	UUID accountId,
	String asset,
	BigDecimal amount
) {

	public static ReserveOrderEvent of(Order order, AssetReserveResult assetReserveResult) {
		return new ReserveOrderEvent(
			order.getOrderId().value(),
			order.getAccountId().value(),
			assetReserveResult.asset(),
			assetReserveResult.amount()
		);
	}
}
