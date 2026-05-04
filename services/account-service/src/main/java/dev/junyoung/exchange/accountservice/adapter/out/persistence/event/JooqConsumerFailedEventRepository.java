package dev.junyoung.exchange.accountservice.adapter.out.persistence.event;


import dev.junyoung.exchange.accountservice.Tables;
import dev.junyoung.exchange.accountservice.application.port.out.ConsumerFailedEventRepository;
import dev.junyoung.exchange.accountservice.domain.model.entity.ConsumerFailedEvent;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JooqConsumerFailedEventRepository implements ConsumerFailedEventRepository {

    private final DSLContext dslContext;

    @Override
    public void save(ConsumerFailedEvent consumerFailedEvent) {
        dslContext.insertInto(Tables.CONSUMER_FAILED_EVENTS)
            .set(JooqConsumerFailedEventMapper.toRecord(dslContext, consumerFailedEvent))
            .onConflictDoNothing()
            .execute();
    }
}
