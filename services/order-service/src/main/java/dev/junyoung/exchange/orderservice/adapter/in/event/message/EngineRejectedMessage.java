package dev.junyoung.exchange.orderservice.adapter.in.event.message;

import dev.junyoung.exchange.orderservice.domain.model.enums.RejectReason;

import java.util.UUID;

public record EngineRejectedMessage(UUID orderId, UUID accountId, RejectReason reason) {
}
