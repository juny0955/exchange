package dev.junyoung.exchange.accountservice.adapter.out.persistence.reservation;

import org.jooq.DSLContext;

import dev.junyoung.exchange.accountservice.Tables;
import dev.junyoung.exchange.accountservice.domain.model.entity.Reservation;
import dev.junyoung.exchange.accountservice.domain.model.enums.ReservationStatus;
import dev.junyoung.exchange.accountservice.domain.model.value.AccountId;
import dev.junyoung.exchange.accountservice.domain.model.value.AssetCode;
import dev.junyoung.exchange.accountservice.domain.model.value.OrderId;
import dev.junyoung.exchange.accountservice.domain.model.value.ReservationId;
import dev.junyoung.exchange.accountservice.tables.records.ReservationsRecord;

final class JooqReservationMapper {

    static ReservationsRecord toRecord(DSLContext dslContext, Reservation reservation) {
        ReservationsRecord record = dslContext.newRecord(Tables.RESERVATIONS);
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

    static Reservation toDomain(ReservationsRecord record) {
        return new Reservation(
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
