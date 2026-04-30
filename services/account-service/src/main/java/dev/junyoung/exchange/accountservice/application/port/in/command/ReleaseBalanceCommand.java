package dev.junyoung.exchange.accountservice.application.port.in.command;

import dev.junyoung.exchange.accountservice.domain.model.value.AccountId;
import dev.junyoung.exchange.accountservice.domain.model.value.OrderId;

public record ReleaseBalanceCommand(AccountId accountId, OrderId orderId) {
}
