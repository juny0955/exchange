package dev.junyoung.exchange.accountservice.domain.model.value;

import dev.junyoung.exchange.accountservice.domain.exception.AccountInvalidException;

public record AssetCode(String value) {
    public AssetCode {
        if (value == null || value.isBlank()) throw new AccountInvalidException("자산 코드는 필수입니다.");
        if (value.length() > 8) throw new AccountInvalidException("자산 코드는 8자 이하여야 합니다.");
        value = value.toUpperCase();
    }

    public static AssetCode from(String raw) {
        return new AssetCode(raw);
    }
}
