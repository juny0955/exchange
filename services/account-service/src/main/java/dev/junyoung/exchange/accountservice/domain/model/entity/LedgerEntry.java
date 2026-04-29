package dev.junyoung.exchange.accountservice.domain.model.entity;

import dev.junyoung.exchange.accountservice.domain.exception.AccountInvalidException;
import dev.junyoung.exchange.accountservice.domain.model.enums.BalanceType;
import dev.junyoung.exchange.accountservice.domain.model.enums.EntryType;
import dev.junyoung.exchange.accountservice.domain.model.enums.ReferenceType;
import dev.junyoung.exchange.accountservice.domain.model.value.AccountId;
import dev.junyoung.exchange.accountservice.domain.model.value.AssetCode;
import dev.junyoung.exchange.accountservice.domain.model.value.LedgerEntryId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * 불변 원장 레코드. updated_at 없음 — 한 번 기록되면 변경 불가.
 * ledgerEntryId는 DB 영속 전 null (BIGSERIAL 자동 채번).
 */
public record LedgerEntry(
    LedgerEntryId ledgerEntryId,
    AccountId accountId,
    AssetCode assetCode,
    BigDecimal amount,
    BalanceType balanceType,
    EntryType entryType,
    ReferenceType referenceType,
    UUID referenceId,
    Instant createdAt
) {
    public LedgerEntry {
        if (accountId == null) throw new AccountInvalidException("계좌 ID는 필수입니다.");
        if (assetCode == null) throw new AccountInvalidException("자산 코드는 필수입니다.");
        if (amount == null || amount.signum() < 0) throw new AccountInvalidException("원장 금액은 0 이상이어야 합니다.");
        if (balanceType == null) throw new AccountInvalidException("잔고 유형은 필수입니다.");
        if (entryType == null) throw new AccountInvalidException("항목 유형은 필수입니다.");
        if (referenceType == null) throw new AccountInvalidException("참조 유형은 필수입니다.");
        if (referenceId == null) throw new AccountInvalidException("참조 ID는 필수입니다.");
    }

    public static LedgerEntry create(
        AccountId accountId,
        AssetCode assetCode,
        BigDecimal amount,
        BalanceType balanceType,
        EntryType entryType,
        ReferenceType referenceType,
        UUID referenceId
    ) {
        return new LedgerEntry(null, accountId, assetCode, amount, balanceType, entryType, referenceType, referenceId, Instant.now());
    }
}
