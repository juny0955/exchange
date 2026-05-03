package dev.junyoung.exchange.orderservice.application.port.in.engine;

import dev.junyoung.exchange.orderservice.domain.model.enums.EngineRejectReason;
import dev.junyoung.exchange.orderservice.domain.model.value.AccountId;
import dev.junyoung.exchange.orderservice.domain.model.value.OrderId;

public interface HandleEngineRejectedEventUseCase {
	void handle(OrderId orderId, AccountId accountId, EngineRejectReason reason);
}
