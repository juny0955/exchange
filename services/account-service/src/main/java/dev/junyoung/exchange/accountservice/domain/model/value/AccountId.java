package dev.junyoung.exchange.accountservice.domain.model.value;

import dev.junyoung.exchange.accountservice.domain.exception.AccountInvalidException;

import java.util.UUID;

public record AccountId(UUID value) {
    public AccountId {
        if (value == null) throw new AccountInvalidException("계좌 ID는 필수입니다.");
    }

    public static AccountId newId() {
        return new AccountId(UUID.randomUUID());
    }
}
