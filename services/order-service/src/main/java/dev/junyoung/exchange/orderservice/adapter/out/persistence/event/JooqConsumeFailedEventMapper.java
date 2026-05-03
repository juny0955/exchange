package dev.junyoung.exchange.orderservice.adapter.out.persistence.event;

import org.jooq.DSLContext;
import org.jooq.JSON;

import dev.junyoung.exchange.orderservice.Tables;
import dev.junyoung.exchange.orderservice.domain.model.entity.ConsumeFailedEvent;
import dev.junyoung.exchange.orderservice.tables.records.ConsumerFailedEventsRecord;

public final class JooqConsumeFailedEventMapper {
    public static ConsumerFailedEventsRecord toRecord(DSLContext dslContext, ConsumeFailedEvent event) {
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
