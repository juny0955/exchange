package dev.junyoung.exchange.orderservice.application.port.in.account;

import dev.junyoung.exchange.orderservice.domain.model.value.AccountId;
import dev.junyoung.exchange.orderservice.domain.model.value.OrderId;

public interface HandleAccountReservedEvent {
	void handle(OrderId orderId, AccountId accountId);
}
