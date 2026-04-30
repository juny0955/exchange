package dev.junyoung.exchange.accountservice.domain.model.value;

import dev.junyoung.exchange.accountservice.domain.exception.AccountInvalidException;

public record LedgerEntryId(Long value) {
    public LedgerEntryId {
        if (value == null) throw new AccountInvalidException("원장 항목 ID는 필수입니다.");
    }
}
