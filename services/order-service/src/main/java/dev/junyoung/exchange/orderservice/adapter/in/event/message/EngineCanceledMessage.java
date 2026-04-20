package dev.junyoung.exchange.orderservice.adapter.in.event.message;

import java.util.UUID;

public record EngineCanceledMessage(UUID orderId, UUID accountId) {
}
