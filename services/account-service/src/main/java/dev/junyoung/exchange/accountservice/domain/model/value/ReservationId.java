package dev.junyoung.exchange.accountservice.domain.model.value;

import dev.junyoung.exchange.accountservice.domain.exception.AccountInvalidException;

public record ReservationId(Long value) {
    public ReservationId {
        if (value == null) throw new AccountInvalidException("예약 ID는 필수입니다.");
    }
}
