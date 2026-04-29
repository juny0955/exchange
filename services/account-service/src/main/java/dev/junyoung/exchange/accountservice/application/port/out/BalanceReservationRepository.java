package dev.junyoung.exchange.accountservice.application.port.out;

import dev.junyoung.exchange.accountservice.domain.model.entity.BalanceReservation;
import dev.junyoung.exchange.accountservice.domain.model.value.AccountId;
import dev.junyoung.exchange.accountservice.domain.model.value.OrderId;

import java.util.List;
import java.util.Optional;

public interface BalanceReservationRepository {
    void save(BalanceReservation balanceReservation);
    void update(BalanceReservation reservation);
    void updateAll(List<BalanceReservation> reservations);

    Optional<BalanceReservation> findByOrderIdAndAccountIdForUpdate(OrderId orderId, AccountId accountId);
}
