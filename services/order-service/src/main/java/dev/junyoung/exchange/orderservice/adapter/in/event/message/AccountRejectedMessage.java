package dev.junyoung.exchange.orderservice.adapter.in.event.message;

import java.util.UUID;

import dev.junyoung.exchange.orderservice.domain.model.enums.AccountRejectReason;

public record AccountRejectedMessage(
	UUID orderId,
	UUID accountId,
	AccountRejectReason reason
) {
}
