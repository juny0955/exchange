package dev.junyoung.exchange.orderservice.application.port.out.command;

import java.util.UUID;

public record CancelOrderEvent(
    UUID orderId,
    UUID accountId,
    OrderSymbolEvent symbol
) {
}
