package dev.junyoung.exchange.orderservice.application.port.out.command;

import java.util.UUID;

public record EngineCancelCommand(
    UUID orderId,
    UUID accountId
) {
}
