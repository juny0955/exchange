package dev.junyoung.exchange.accountservice.adapter.out.persistence.reservation;

import dev.junyoung.exchange.accountservice.Tables;
import dev.junyoung.exchange.accountservice.application.port.out.ReservationRepository;
import dev.junyoung.exchange.accountservice.domain.model.entity.Reservation;
import dev.junyoung.exchange.accountservice.domain.model.value.AccountId;
import dev.junyoung.exchange.accountservice.domain.model.value.OrderId;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JooqReservationRepository implements ReservationRepository {

    private final DSLContext dslContext;

    @Override
    public void save(Reservation reservation) {
        dslContext.insertInto(Tables.RESERVATIONS)
            .set(JooqReservationMapper.toRecord(dslContext, reservation))
            .execute();
    }

    @Override
    public void update(Reservation reservation) {
        dslContext.update(Tables.RESERVATIONS)
            .set(Tables.RESERVATIONS.RELEASED_AMOUNT, reservation.getReleasedAmount())
            .set(Tables.RESERVATIONS.STATUS, reservation.getStatus().name())
            .set(Tables.RESERVATIONS.UPDATED_AT, reservation.getUpdatedAt())
            .where(Tables.RESERVATIONS.ORDER_ID.eq(reservation.getOrderId().value()))
            .and(Tables.RESERVATIONS.ACCOUNT_ID.eq(reservation.getAccountId().value()))
            .execute();
    }

    @Override
    public void updateAll(List<Reservation> reservations) {
        if (reservations.isEmpty()) return;

        var queries = reservations.stream()
            .map(reservation -> dslContext.update(Tables.RESERVATIONS)
                .set(Tables.RESERVATIONS.RELEASED_AMOUNT, reservation.getReleasedAmount())
                .set(Tables.RESERVATIONS.STATUS, reservation.getStatus().name())
                .set(Tables.RESERVATIONS.UPDATED_AT, reservation.getUpdatedAt())
                .where(Tables.RESERVATIONS.ORDER_ID.eq(reservation.getOrderId().value()))
                .and(Tables.RESERVATIONS.ACCOUNT_ID.eq(reservation.getAccountId().value())))
            .toList();

        dslContext.batch(queries).execute();
    }

    @Override
    public Optional<Reservation> findByOrderIdAndAccountIdForUpdate(OrderId orderId, AccountId accountId) {
        return Optional.ofNullable(
            dslContext.selectFrom(Tables.RESERVATIONS)
                .where(Tables.RESERVATIONS.ORDER_ID.eq(orderId.value()))
                .and(Tables.RESERVATIONS.ACCOUNT_ID.eq(accountId.value()))
                .forUpdate()
                .fetchOne(JooqReservationMapper::toDomain)
        );
    }
}
