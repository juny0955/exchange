package dev.junyoung.exchange.orderservice.application.port.in.command;

import dev.junyoung.exchange.orderservice.domain.model.value.AccountId;
import dev.junyoung.exchange.orderservice.domain.model.value.OrderId;

import java.util.UUID;

public record CancelOrderCommand(
    OrderId  orderId,
    AccountId accountId
) {
    public CancelOrderCommand(UUID orderId, UUID accountId) {
        this(new OrderId(orderId), new AccountId(accountId));
    }
}
