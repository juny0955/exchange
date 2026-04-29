package dev.junyoung.exchange.accountservice.adapter.out.persistence.balanceReservation;

import dev.junyoung.exchange.accountservice.Tables;
import dev.junyoung.exchange.accountservice.application.port.out.BalanceReservationRepository;
import dev.junyoung.exchange.accountservice.domain.model.entity.BalanceReservation;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

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
}
