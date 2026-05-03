package dev.junyoung.exchange.orderservice.adapter.out.persistence.event;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import dev.junyoung.exchange.orderservice.Tables;
import dev.junyoung.exchange.orderservice.application.port.out.ConsumerFailedEventRepository;
import dev.junyoung.exchange.orderservice.domain.model.entity.ConsumerFailedEvent;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class JooqConsumeFailedEventRepository implements ConsumerFailedEventRepository {

    private final DSLContext dslContext;

    @Override
    public void save(ConsumerFailedEvent consumerFailedEvent) {
        dslContext.insertInto(Tables.CONSUMER_FAILED_EVENTS)
            .set(JooqConsumerFailedEventMapper.toRecord(dslContext, consumerFailedEvent))
            .onConflictDoNothing()
            .execute();
    }
}
