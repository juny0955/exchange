package dev.junyoung.exchange.accountservice.adapter.in.event.message;

import java.util.UUID;

public record EngineRejectedMessage(
    UUID accountId,
    UUID orderId
) {
}
