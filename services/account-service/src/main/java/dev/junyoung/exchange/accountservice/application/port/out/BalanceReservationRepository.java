package dev.junyoung.exchange.accountservice.application.port.out;

import dev.junyoung.exchange.accountservice.domain.model.entity.BalanceReservation;

public interface BalanceReservationRepository {
    void save(BalanceReservation balanceReservation);
}
