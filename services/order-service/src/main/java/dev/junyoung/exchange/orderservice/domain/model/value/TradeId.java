package dev.junyoung.exchange.orderservice.domain.model.value;

import java.util.Objects;
import java.util.UUID;

public record TradeId(
	UUID value
) {
	public TradeId {
		Objects.requireNonNull(value, "체결 ID는 필수 입니다.");
	}
}
