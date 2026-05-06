package dev.junyoung.exchange.accountservice.domain.model.entity;

import java.time.Instant;

import dev.junyoung.exchange.accountservice.domain.model.enums.EventType;
import dev.junyoung.exchange.accountservice.domain.model.enums.OutboxStatus;
import dev.junyoung.exchange.accountservice.domain.model.value.AccountId;
import dev.junyoung.exchange.accountservice.domain.model.value.OrderId;
import dev.junyoung.exchange.accountservice.domain.model.value.OutboxId;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AccountOutbox {
    private final OutboxId outboxId;
    private final String aggregateType;
    private final AccountId aggregateId;
    private final EventType eventType;
    private final OrderId referenceId;
    private final String referenceType;
    private final String partitionKey;
    private final String payload;
    private OutboxStatus status;
    private int retryCount;
    private final Instant createdAt;
    private Instant publishedAt;

    private static final String AGGREGATE_TYPE = "ACCOUNT";
    private static final String REFERENCE_TYPE = "ORDER";

    public static AccountOutbox create(AccountId aggregateId, EventType eventType, OrderId referenceId, String payload) {
        return new AccountOutbox(
            OutboxId.newId(),
            AGGREGATE_TYPE,
            aggregateId,
            eventType,
            referenceId,
            REFERENCE_TYPE,
            referenceId.value().toString(),
            payload,
            OutboxStatus.PENDING,
            0,
            Instant.now(),
            null
        );
    }

    public boolean complete(Instant publishedAt) {
        if (OutboxStatus.SUCCESS.equals(status))
            return false;

        status = OutboxStatus.SUCCESS;
        this.publishedAt = publishedAt;
        return true;
    }
}
