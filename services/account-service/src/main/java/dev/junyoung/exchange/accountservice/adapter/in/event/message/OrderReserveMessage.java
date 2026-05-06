package dev.junyoung.exchange.accountservice.adapter.in.event.message;

import java.math.BigDecimal;
import java.util.UUID;

import dev.junyoung.exchange.accountservice.application.port.in.command.ReserveBalanceCommand;
import dev.junyoung.exchange.accountservice.application.port.in.command.SaveRejectedOutboxCommand;
import dev.junyoung.exchange.accountservice.domain.model.enums.RejectedReason;
import dev.junyoung.exchange.accountservice.domain.model.value.AccountId;
import dev.junyoung.exchange.accountservice.domain.model.value.AssetCode;
import dev.junyoung.exchange.accountservice.domain.model.value.OrderId;

public record OrderReserveMessage(
	UUID orderId,
	UUID accountId,
	String asset,
	BigDecimal amount
) {
	public ReserveBalanceCommand toCommand() {
		return new ReserveBalanceCommand(
			new OrderId(orderId),
			new AccountId(accountId),
			new AssetCode(asset),
			amount
		);
	}

	public SaveRejectedOutboxCommand toRejectedCommand(RejectedReason reason) {
		return new SaveRejectedOutboxCommand(
			new OrderId(orderId),
			new AccountId(accountId),
			new AssetCode(asset),
			reason
		);
	}
}
