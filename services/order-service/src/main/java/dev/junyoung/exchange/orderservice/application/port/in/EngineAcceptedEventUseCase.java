package dev.junyoung.exchange.orderservice.application.port.in;

import dev.junyoung.exchange.orderservice.domain.model.value.AccountId;
import dev.junyoung.exchange.orderservice.domain.model.value.OrderId;

public interface EngineAcceptedEventUseCase {
	void accepted(OrderId orderId, AccountId accountId);
}
