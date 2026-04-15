package dev.junyoung.exchange.orderservice.domain.model.value;

import dev.junyoung.exchange.orderservice.domain.exception.OrderInvalidException;

import java.util.UUID;

public record TradeId(
	UUID value
) {
	public TradeId {
		if (value == null)
			throw new OrderInvalidException("체결 ID는 필수 입니다.");
	}
}
