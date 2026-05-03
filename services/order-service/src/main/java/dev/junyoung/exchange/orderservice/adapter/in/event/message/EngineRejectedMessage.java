package dev.junyoung.exchange.orderservice.adapter.in.event.message;

import dev.junyoung.exchange.orderservice.domain.model.enums.EngineRejectReason;

import java.util.UUID;

public record EngineRejectedMessage(UUID orderId, UUID accountId, EngineRejectReason reason) {
}
