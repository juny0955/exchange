package dev.junyoung.exchange.orderservice.adapter.in.event.message;

import java.util.UUID;

public record EngineRejectedMessage(UUID orderId, UUID accountId) {
}
