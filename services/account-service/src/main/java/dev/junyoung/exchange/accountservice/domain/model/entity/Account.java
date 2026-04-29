package dev.junyoung.exchange.accountservice.domain.model.entity;

import dev.junyoung.exchange.accountservice.domain.exception.AccountStateConflictException;
import dev.junyoung.exchange.accountservice.domain.model.enums.AccountStatus;
import dev.junyoung.exchange.accountservice.domain.model.value.AccountId;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class Account {
    private final AccountId accountId;
    private AccountStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    public static Account create() {
        Instant now = Instant.now();
        return new Account(AccountId.newId(), AccountStatus.ACTIVE, now, now);
    }

    public void suspend() {
        if (status == AccountStatus.CLOSED) throw new AccountStateConflictException("이미 종료된 계좌입니다.");
        if (status == AccountStatus.SUSPENDED) throw new AccountStateConflictException("이미 정지된 계좌입니다.");
        status = AccountStatus.SUSPENDED;
        updatedAt = Instant.now();
    }

    public void activate() {
        if (status != AccountStatus.SUSPENDED) throw new AccountStateConflictException("정지된 계좌가 아닙니다.");
        status = AccountStatus.ACTIVE;
        updatedAt = Instant.now();
    }

    public void close() {
        if (status == AccountStatus.CLOSED) throw new AccountStateConflictException("이미 종료된 계좌입니다.");
        status = AccountStatus.CLOSED;
        updatedAt = Instant.now();
    }

    public boolean isActive() {
        return status == AccountStatus.ACTIVE;
    }
}
