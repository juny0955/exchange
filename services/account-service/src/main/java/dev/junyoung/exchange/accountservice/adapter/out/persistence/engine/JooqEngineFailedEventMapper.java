package dev.junyoung.exchange.accountservice.adapter.out.persistence.engine;

import dev.junyoung.exchange.accountservice.Tables;
import dev.junyoung.exchange.accountservice.domain.model.entity.EngineFailedEvent;
import dev.junyoung.exchange.accountservice.tables.records.EngineFailedEventRecord;
import org.jooq.DSLContext;
import org.jooq.JSON;

public final class JooqEngineFailedEventMapper {
    public static EngineFailedEventRecord toRecord(DSLContext dslContext, EngineFailedEvent event) {
        EngineFailedEventRecord record = dslContext.newRecord(Tables.ENGINE_FAILED_EVENT);
        record.setTopic(event.getTopic());
        record.setPartition(event.getPartition());
        record.setEventOffset(event.getOffset());
        record.setPayload(JSON.json(event.getPayload()));
        record.setErrorMessage(event.getErrorMessage());
        record.setStatus(event.getStatus().name());
        record.setRetryCount(event.getRetryCount());
        record.setFailedAt(event.getFailedAt());
        record.setResolvedAt(event.getResolvedAt());
        return record;
    }
}
