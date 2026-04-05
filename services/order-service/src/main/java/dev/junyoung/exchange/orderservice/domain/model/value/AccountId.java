package dev.junyoung.exchange.orderservice.domain.model.value;

import java.util.UUID;

import dev.junyoung.exchange.core.exception.InvalidDomainException;

public record AccountId(
	UUID value
) {
	public AccountId {
		if (value == null)
			throw new InvalidDomainException("계좌 ID는 필수 입니다.");
	}
}
