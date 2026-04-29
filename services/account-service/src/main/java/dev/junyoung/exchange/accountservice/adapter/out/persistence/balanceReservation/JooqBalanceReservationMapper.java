package dev.junyoung.exchange.accountservice.adapter.out.persistence.balanceReservation;

import dev.junyoung.exchange.accountservice.Tables;
import dev.junyoung.exchange.accountservice.domain.model.entity.BalanceReservation;
import dev.junyoung.exchange.accountservice.domain.model.enums.ReservationStatus;
import dev.junyoung.exchange.accountservice.domain.model.value.AccountId;
import dev.junyoung.exchange.accountservice.domain.model.value.AssetCode;
import dev.junyoung.exchange.accountservice.domain.model.value.OrderId;
import dev.junyoung.exchange.accountservice.domain.model.value.ReservationId;
import dev.junyoung.exchange.accountservice.tables.records.BalanceReservationsRecord;
import org.jooq.DSLContext;

final class JooqBalanceReservationMapper {

    static BalanceReservationsRecord toRecord(DSLContext dslContext, BalanceReservation reservation) {
        BalanceReservationsRecord record = dslContext.newRecord(Tables.BALANCE_RESERVATIONS);
        record.setOrderId(reservation.getOrderId().value());
        record.setAccountId(reservation.getAccountId().value());
        record.setAssetCode(reservation.getAssetCode().value());
        record.setAmount(reservation.getAmount());
        record.setReleasedAmount(reservation.getReleasedAmount());
        record.setStatus(reservation.getStatus().name());
        record.setCreatedAt(reservation.getCreatedAt());
        record.setUpdatedAt(reservation.getUpdatedAt());
        return record;
    }

    static BalanceReservation toDomain(BalanceReservationsRecord record) {
        return new BalanceReservation(
            new ReservationId(record.getReservationId()),
            new OrderId(record.getOrderId()),
            new AccountId(record.getAccountId()),
            new AssetCode(record.getAssetCode()),
            record.getAmount(),
            record.getReleasedAmount(),
            ReservationStatus.valueOf(record.getStatus()),
            record.getCreatedAt(),
            record.getUpdatedAt()
        );
    }
}
