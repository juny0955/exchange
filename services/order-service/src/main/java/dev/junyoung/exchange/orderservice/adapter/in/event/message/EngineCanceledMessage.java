package dev.junyoung.exchange.orderservice.adapter.in.event.message;

import dev.junyoung.exchange.orderservice.domain.model.enums.CancelReason;

import java.util.UUID;

public record EngineCanceledMessage(UUID orderId, UUID accountId, CancelReason reason) {
}
