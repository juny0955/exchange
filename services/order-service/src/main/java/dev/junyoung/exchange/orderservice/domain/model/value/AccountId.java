package dev.junyoung.exchange.orderservice.domain.model.value;

import java.util.Objects;
import java.util.UUID;

public record AccountId(
	UUID value
) {
	public AccountId {
		Objects.requireNonNull(value, "계좌 ID는 필수 입니다.");
	}
}
