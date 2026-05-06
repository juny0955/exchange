package dev.junyoung.exchange.accountservice.adapter.out.persistence.outbox;

import dev.junyoung.exchange.accountservice.Tables;
import dev.junyoung.exchange.accountservice.domain.model.entity.AccountOutbox;
import dev.junyoung.exchange.accountservice.domain.model.enums.EventType;
import dev.junyoung.exchange.accountservice.domain.model.enums.OutboxStatus;
import dev.junyoung.exchange.accountservice.domain.model.value.AccountId;
import dev.junyoung.exchange.accountservice.domain.model.value.OrderId;
import dev.junyoung.exchange.accountservice.domain.model.value.OutboxId;
import dev.junyoung.exchange.accountservice.tables.records.AccountOutboxRecord;
import org.jooq.DSLContext;
import org.jooq.JSON;

public final class JooqAccountOutboxMapper {

    public static AccountOutboxRecord toRecord(DSLContext dslContext, AccountOutbox outbox) {
        AccountOutboxRecord record = dslContext.newRecord(Tables.ACCOUNT_OUTBOX);
        record.setOutboxId(outbox.getOutboxId().value());
        record.setAggregateType(outbox.getAggregateType());
        record.setAggregateId(outbox.getAggregateId().value());
        record.setEventType(outbox.getEventType().name());
        record.setReferenceId(outbox.getReferenceId().value());
        record.setReferenceType(outbox.getReferenceType());
        record.setPartitionKey(outbox.getPartitionKey());
        record.setPayload(JSON.json(outbox.getPayload()));
        record.setStatus(outbox.getStatus().name());
        record.setRetryCount(outbox.getRetryCount());
        record.setCreatedAt(outbox.getCreatedAt());
        record.setPublishedAt(outbox.getPublishedAt());
        return record;
    }

    public static AccountOutbox toDomain(AccountOutboxRecord record) {
        return new AccountOutbox(
            new OutboxId(record.getOutboxId()),
            record.getAggregateType(),
            new AccountId(record.getAggregateId()),
            EventType.valueOf(record.getEventType()),
            new OrderId(record.getReferenceId()),
            record.getReferenceType(),
            record.getPartitionKey(),
            record.getPayload().data(),
            OutboxStatus.valueOf(record.getStatus()),
            record.getRetryCount(),
            record.getCreatedAt(),
            record.getPublishedAt()
        );
    }
}
