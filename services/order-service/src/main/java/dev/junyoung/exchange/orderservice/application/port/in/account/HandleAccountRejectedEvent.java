package dev.junyoung.exchange.orderservice.application.port.in.account;

import dev.junyoung.exchange.orderservice.domain.model.enums.AccountRejectReason;
import dev.junyoung.exchange.orderservice.domain.model.value.AccountId;
import dev.junyoung.exchange.orderservice.domain.model.value.OrderId;

public interface HandleAccountRejectedEvent {
	void handle(OrderId orderId, AccountId accountId, AccountRejectReason reason);
}
