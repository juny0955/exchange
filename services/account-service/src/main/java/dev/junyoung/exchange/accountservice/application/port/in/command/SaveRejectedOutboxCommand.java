package dev.junyoung.exchange.accountservice.application.port.in.command;

import dev.junyoung.exchange.accountservice.domain.model.enums.RejectedReason;
import dev.junyoung.exchange.accountservice.domain.model.value.AccountId;
import dev.junyoung.exchange.accountservice.domain.model.value.AssetCode;
import dev.junyoung.exchange.accountservice.domain.model.value.OrderId;

public record SaveRejectedOutboxCommand(
    OrderId orderId,
    AccountId accountId,
    AssetCode assetCode,
    RejectedReason reason
) {
}
