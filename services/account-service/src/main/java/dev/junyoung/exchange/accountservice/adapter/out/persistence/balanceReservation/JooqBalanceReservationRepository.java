package dev.junyoung.exchange.accountservice.adapter.out.persistence.balanceReservation;

import dev.junyoung.exchange.accountservice.Tables;
import dev.junyoung.exchange.accountservice.application.port.out.BalanceReservationRepository;
import dev.junyoung.exchange.accountservice.domain.model.entity.BalanceReservation;
import dev.junyoung.exchange.accountservice.domain.model.value.AccountId;
import dev.junyoung.exchange.accountservice.domain.model.value.OrderId;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JooqBalanceReservationRepository implements BalanceReservationRepository {

    private final DSLContext dslContext;

    @Override
    public void save(BalanceReservation reservation) {
        dslContext.insertInto(Tables.BALANCE_RESERVATIONS)
            .set(JooqBalanceReservationMapper.toRecord(dslContext, reservation))
            .execute();
    }

    @Override
    public void update(BalanceReservation reservation) {
        dslContext.update(Tables.BALANCE_RESERVATIONS)
            .set(Tables.BALANCE_RESERVATIONS.RELEASED_AMOUNT, reservation.getReleasedAmount())
            .set(Tables.BALANCE_RESERVATIONS.STATUS, reservation.getStatus().name())
            .set(Tables.BALANCE_RESERVATIONS.UPDATED_AT, reservation.getUpdatedAt())
            .where(Tables.BALANCE_RESERVATIONS.ORDER_ID.eq(reservation.getOrderId().value()))
            .and(Tables.BALANCE_RESERVATIONS.ACCOUNT_ID.eq(reservation.getAccountId().value()))
            .execute();
    }

    @Override
    public void updateAll(List<BalanceReservation> reservations) {
        if (reservations.isEmpty()) return;

        var queries = reservations.stream()
            .map(reservation -> dslContext.update(Tables.BALANCE_RESERVATIONS)
                .set(Tables.BALANCE_RESERVATIONS.RELEASED_AMOUNT, reservation.getReleasedAmount())
                .set(Tables.BALANCE_RESERVATIONS.STATUS, reservation.getStatus().name())
                .set(Tables.BALANCE_RESERVATIONS.UPDATED_AT, reservation.getUpdatedAt())
                .where(Tables.BALANCE_RESERVATIONS.ORDER_ID.eq(reservation.getOrderId().value()))
                .and(Tables.BALANCE_RESERVATIONS.ACCOUNT_ID.eq(reservation.getAccountId().value())))
            .toList();

        dslContext.batch(queries).execute();
    }

    @Override
    public Optional<BalanceReservation> findByOrderIdAndAccountIdForUpdate(OrderId orderId, AccountId accountId) {
        return Optional.ofNullable(
            dslContext.selectFrom(Tables.BALANCE_RESERVATIONS)
                .where(Tables.BALANCE_RESERVATIONS.ORDER_ID.eq(orderId.value()))
                .and(Tables.BALANCE_RESERVATIONS.ACCOUNT_ID.eq(accountId.value()))
                .forUpdate()
                .fetchOne(JooqBalanceReservationMapper::toDomain)
        );
    }
}
