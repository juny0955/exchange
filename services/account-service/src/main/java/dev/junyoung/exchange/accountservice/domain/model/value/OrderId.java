package dev.junyoung.exchange.accountservice.domain.model.value;

import dev.junyoung.exchange.accountservice.domain.exception.AccountInvalidException;

import java.util.UUID;

public record OrderId(UUID value) {
    public OrderId {
        if (value == null) throw new AccountInvalidException("주문 ID는 필수입니다.");
    }

    public static OrderId from(String raw) {
        return new OrderId(UUID.fromString(raw));
    }
}
