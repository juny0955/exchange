package dev.junyoung.exchange.accountservice.domain.model.entity;

import dev.junyoung.exchange.accountservice.domain.exception.AccountInvalidException;
import dev.junyoung.exchange.accountservice.domain.exception.AccountStateConflictException;
import dev.junyoung.exchange.accountservice.domain.model.enums.ReservationStatus;
import dev.junyoung.exchange.accountservice.domain.model.value.AccountId;
import dev.junyoung.exchange.accountservice.domain.model.value.AssetCode;
import dev.junyoung.exchange.accountservice.domain.model.value.OrderId;
import dev.junyoung.exchange.accountservice.domain.model.value.ReservationId;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@AllArgsConstructor
public class BalanceReservation {
    private final ReservationId reservationId;
    private final OrderId orderId;
    private final AccountId accountId;
    private final AssetCode assetCode;
    private final BigDecimal amount;
    private BigDecimal releasedAmount;
    private ReservationStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    public static BalanceReservation create(
        OrderId orderId,
        AccountId accountId,
        AssetCode assetCode,
        BigDecimal amount
    ) {
        if (amount == null || amount.signum() <= 0)
            throw new AccountInvalidException("예약 금액은 0보다 커야 합니다.");
        Instant now = Instant.now();
        return new BalanceReservation(null, orderId, accountId, assetCode, amount, BigDecimal.ZERO, ReservationStatus.ACTIVE, now, now);
    }

    public void release(BigDecimal releaseAmount) {
        if (status == ReservationStatus.RELEASED) throw new AccountStateConflictException("이미 해제된 예약입니다.");
        if (releaseAmount == null || releaseAmount.signum() <= 0)
            throw new AccountInvalidException("해제 금액은 0보다 커야 합니다.");

        BigDecimal newReleased = releasedAmount.add(releaseAmount);
        if (newReleased.compareTo(amount) > 0)
            throw new AccountStateConflictException("해제 금액이 예약 금액을 초과합니다.");

        releasedAmount = newReleased;
        status = newReleased.compareTo(amount) == 0
            ? ReservationStatus.RELEASED
            : ReservationStatus.PARTIALLY_RELEASED;
        updatedAt = Instant.now();
    }

    public BigDecimal getRemainingHeld() {
        return amount.subtract(releasedAmount);
    }

    public boolean isFullyReleased() {
        return status == ReservationStatus.RELEASED;
    }
}
