package dev.junyoung.exchange.accountservice.application.port.in.command;

import dev.junyoung.exchange.accountservice.domain.model.value.AccountId;
import dev.junyoung.exchange.accountservice.domain.model.value.AssetCode;
import dev.junyoung.exchange.accountservice.domain.model.value.OrderId;

import java.math.BigDecimal;

public record ReserveBalanceCommand(
    OrderId orderId,
    AccountId accountId,
    AssetCode assetCode,
    BigDecimal amount
) {
}
