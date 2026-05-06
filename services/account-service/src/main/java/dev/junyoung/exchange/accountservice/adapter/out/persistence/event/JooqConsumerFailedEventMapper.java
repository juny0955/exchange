package dev.junyoung.exchange.accountservice.adapter.out.persistence.event;

import dev.junyoung.exchange.accountservice.Tables;
import dev.junyoung.exchange.accountservice.domain.model.entity.ConsumerFailedEvent;
import dev.junyoung.exchange.accountservice.tables.records.ConsumerFailedEventsRecord;
import org.jooq.DSLContext;
import org.jooq.JSON;

public final class JooqConsumerFailedEventMapper {
    public static ConsumerFailedEventsRecord toRecord(DSLContext dslContext, ConsumerFailedEvent event) {
        ConsumerFailedEventsRecord record = dslContext.newRecord(Tables.CONSUMER_FAILED_EVENTS);
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
