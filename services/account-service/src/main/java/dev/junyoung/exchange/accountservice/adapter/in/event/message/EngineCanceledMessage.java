package dev.junyoung.exchange.accountservice.adapter.in.event.message;

import java.util.UUID;

public record EngineCanceledMessage(
    UUID accountId,
    UUID orderId
) {
}
