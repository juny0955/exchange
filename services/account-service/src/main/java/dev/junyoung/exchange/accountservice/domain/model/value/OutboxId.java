package dev.junyoung.exchange.accountservice.domain.model.value;

import dev.junyoung.exchange.accountservice.domain.exception.AccountInvalidException;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public record OutboxId(UUID value) {
    public OutboxId {
        if (value == null) throw new AccountInvalidException("아웃박스 ID는 필수입니다.");
    }

    public static OutboxId newId() {
        return new OutboxId(UUID.randomUUID());
    }

    public static OutboxId from(byte[] value) {
        return new OutboxId(UUID.fromString(new String(value, StandardCharsets.UTF_8)));
    }
}
