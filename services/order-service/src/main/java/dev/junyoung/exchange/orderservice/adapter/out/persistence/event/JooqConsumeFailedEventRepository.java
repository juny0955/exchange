package dev.junyoung.exchange.orderservice.adapter.out.persistence.event;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import dev.junyoung.exchange.orderservice.Tables;
import dev.junyoung.exchange.orderservice.application.port.out.ConsumeFailedEventRepository;
import dev.junyoung.exchange.orderservice.domain.model.entity.ConsumeFailedEvent;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class JooqConsumeFailedEventRepository implements ConsumeFailedEventRepository {

    private final DSLContext dslContext;

    @Override
    public void save(ConsumeFailedEvent consumeFailedEvent) {
        dslContext.insertInto(Tables.CONSUMER_FAILED_EVENTS)
            .set(JooqConsumeFailedEventMapper.toRecord(dslContext, consumeFailedEvent))
            .onConflictDoNothing()
            .execute();
    }
}
