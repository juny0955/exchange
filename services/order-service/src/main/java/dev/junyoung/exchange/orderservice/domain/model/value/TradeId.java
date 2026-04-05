package dev.junyoung.exchange.orderservice.domain.model.value;

import java.util.UUID;

import dev.junyoung.exchange.core.exception.InvalidDomainException;

public record TradeId(
	UUID value
) {
	public TradeId {
		if (value == null)
			throw new InvalidDomainException("체결 ID는 필수 입니다.");
	}
}
