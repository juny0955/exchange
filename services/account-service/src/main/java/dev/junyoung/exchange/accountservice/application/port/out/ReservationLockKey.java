package dev.junyoung.exchange.accountservice.application.port.out;

import dev.junyoung.exchange.accountservice.domain.model.value.AccountId;
import dev.junyoung.exchange.accountservice.domain.model.value.OrderId;

public record ReservationLockKey(OrderId orderId, AccountId accountId) {
}
