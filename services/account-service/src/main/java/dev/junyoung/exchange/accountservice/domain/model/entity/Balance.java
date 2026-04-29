package dev.junyoung.exchange.accountservice.domain.model.entity;

import dev.junyoung.exchange.accountservice.domain.exception.AccountInvalidException;
import dev.junyoung.exchange.accountservice.domain.exception.AccountStateConflictException;
import dev.junyoung.exchange.accountservice.domain.model.value.AccountId;
import dev.junyoung.exchange.accountservice.domain.model.value.AssetCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@AllArgsConstructor
public class Balance {
    private final AccountId accountId;
    private final AssetCode assetCode;
    private BigDecimal available;
    private BigDecimal held;
    private final Instant createdAt;
    private Instant updatedAt;

    public static Balance createZero(AccountId accountId, AssetCode assetCode) {
        Instant now = Instant.now();
        return new Balance(accountId, assetCode, BigDecimal.ZERO, BigDecimal.ZERO, now, now);
    }

    public void deposit(BigDecimal amount) {
        requirePositive(amount, "입금 금액");
        available = available.add(amount);
        updatedAt = Instant.now();
    }

    public void withdraw(BigDecimal amount) {
        requirePositive(amount, "출금 금액");
        if (available.compareTo(amount) < 0) throw new AccountStateConflictException("가용 잔고가 부족합니다.");
        available = available.subtract(amount);
        updatedAt = Instant.now();
    }

    public void reserve(BigDecimal amount) {
        requirePositive(amount, "예약 금액");
        if (available.compareTo(amount) < 0) throw new AccountStateConflictException("예약을 위한 가용 잔고가 부족합니다.");
        available = available.subtract(amount);
        held = held.add(amount);
        updatedAt = Instant.now();
    }

    public void release(BigDecimal amount) {
        requirePositive(amount, "해제 금액");
        if (held.compareTo(amount) < 0) throw new AccountStateConflictException("보류 잔고가 부족합니다.");
        held = held.subtract(amount);
        available = available.add(amount);
        updatedAt = Instant.now();
    }

    /** 체결 시 held에서 차감 (거래 상대방에게 이전, available로 복귀 없음). */
    public void settleHeld(BigDecimal amount) {
        requirePositive(amount, "정산 금액");
        if (held.compareTo(amount) < 0) throw new AccountStateConflictException("정산을 위한 보류 잔고가 부족합니다.");
        held = held.subtract(amount);
        updatedAt = Instant.now();
    }

    /** 체결 시 available에 입금 (거래 상대방으로부터 수취). */
    public void settleAvailable(BigDecimal amount) {
        requirePositive(amount, "정산 금액");
        available = available.add(amount);
        updatedAt = Instant.now();
    }

    public BigDecimal getTotal() {
        return available.add(held);
    }

    private void requirePositive(BigDecimal amount, String fieldName) {
        if (amount == null || amount.signum() <= 0)
            throw new AccountInvalidException(fieldName + "은(는) 0보다 커야 합니다.");
    }
}
