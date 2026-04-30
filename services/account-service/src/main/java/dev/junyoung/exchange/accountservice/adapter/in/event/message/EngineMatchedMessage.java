package dev.junyoung.exchange.accountservice.adapter.in.event.message;

import java.math.BigDecimal;
import java.util.UUID;

import dev.junyoung.exchange.accountservice.application.port.in.command.SettleBalanceCommand;
import dev.junyoung.exchange.accountservice.domain.model.value.AccountId;
import dev.junyoung.exchange.accountservice.domain.model.value.OrderId;
import dev.junyoung.exchange.accountservice.domain.model.value.TradeId;

public record EngineMatchedMessage(
	UUID tradeId,
	UUID buyAccountId,
	UUID buyOrderId,
	UUID sellAccountId,
	UUID sellOrderId,
	BigDecimal quantity,
	BigDecimal quoteQty
) {
	public SettleBalanceCommand toCommand() {
		return new SettleBalanceCommand(
			new TradeId(tradeId),
			new AccountId(buyAccountId),
			new OrderId(buyOrderId),
			new AccountId(sellAccountId),
			new OrderId(sellOrderId),
			quantity,
			quoteQty
		);
	}
}
