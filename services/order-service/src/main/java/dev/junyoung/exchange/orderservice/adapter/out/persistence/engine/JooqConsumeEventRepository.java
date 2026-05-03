package dev.junyoung.exchange.orderservice.adapter.out.persistence.engine;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import dev.junyoung.exchange.orderservice.Tables;
import dev.junyoung.exchange.orderservice.application.port.out.ConsumeEventRepository;
import dev.junyoung.exchange.orderservice.domain.model.entity.ConsumeFailedEvent;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class JooqConsumeEventRepository implements ConsumeEventRepository {

    private final DSLContext dslContext;

    @Override
    public void save(ConsumeFailedEvent consumeFailedEvent) {
        dslContext.insertInto(Tables.CONSUMER_FAILED_EVENTS)
            .set(JooqConsumeFailedEventMapper.toRecord(dslContext, consumeFailedEvent))
            .onConflictDoNothing()
            .execute();
    }
}
