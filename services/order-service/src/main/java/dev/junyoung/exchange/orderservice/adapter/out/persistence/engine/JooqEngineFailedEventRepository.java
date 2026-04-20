package dev.junyoung.exchange.orderservice.adapter.out.persistence.engine;

import dev.junyoung.exchange.orderservice.Tables;
import dev.junyoung.exchange.orderservice.application.port.out.EngineFailedEventRepository;
import dev.junyoung.exchange.orderservice.domain.model.entity.EngineFailedEvent;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JooqEngineFailedEventRepository implements EngineFailedEventRepository {

    private final DSLContext dslContext;

    @Override
    public void save(EngineFailedEvent engineFailedEvent) {
        dslContext.insertInto(Tables.ENGINE_FAILED_EVENT)
            .set(JooqEngineFailedEventMapper.toRecord(dslContext, engineFailedEvent))
            .onConflictDoNothing()
            .execute();
    }
}
