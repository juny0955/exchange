package dev.junyoung.exchange.accountservice.application.port.in.command;

import dev.junyoung.exchange.accountservice.domain.model.value.AccountId;
import dev.junyoung.exchange.accountservice.domain.model.value.OrderId;
import dev.junyoung.exchange.accountservice.domain.model.value.TradeId;

import java.math.BigDecimal;

public record SettleBalanceCommand(
    TradeId tradeId,
    AccountId buyAccountId,
    OrderId buyOrderId,
    AccountId sellAccountId,
    OrderId sellOrderId,
    BigDecimal quantity,
    BigDecimal quoteQty
) {
}
