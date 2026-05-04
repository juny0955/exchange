package dev.junyoung.exchange.accountservice.application.port.out;

import dev.junyoung.exchange.accountservice.domain.model.entity.Reservation;
import dev.junyoung.exchange.accountservice.domain.model.value.AccountId;
import dev.junyoung.exchange.accountservice.domain.model.value.OrderId;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository {
    void save(Reservation reservation);
    void update(Reservation reservation);
    void updateAll(List<Reservation> reservations);

    Optional<Reservation> findByOrderIdAndAccountIdForUpdate(OrderId orderId, AccountId accountId);
    List<Reservation> findAllByLockKeyForUpdate(List<ReservationLockKey> keys);

    boolean existsOrderIdAndAccountId(OrderId orderId, AccountId accountId);
}
