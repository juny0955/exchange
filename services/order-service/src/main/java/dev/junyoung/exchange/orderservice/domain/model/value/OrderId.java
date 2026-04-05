package dev.junyoung.exchange.orderservice.domain.model.value;

import java.util.Objects;
import java.util.UUID;

public record OrderId(
	UUID value
) {
	public OrderId {
		Objects.requireNonNull(value, "주문 ID는 필수 입니다.");
	}
}
