package dev.junyoung.exchange.accountservice.domain.model.entity;


import dev.junyoung.exchange.accountservice.domain.model.enums.EngineFailedEventStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class EngineFailedEvent {
    private final Long failedId;
    private final String topic;
    private final int partition;
    private final long offset;
    private final String payload;
    private final String errorMessage;
    private EngineFailedEventStatus status;
    private int retryCount;
    private final Instant failedAt;
    private Instant resolvedAt;

    public static EngineFailedEvent create(
        String topic,
        int partition,
        long offset,
        String payload,
        String errorMessage
    ) {
        return new EngineFailedEvent(
            null,
            topic,
            partition,
            offset,
            payload,
            errorMessage,
            EngineFailedEventStatus.PENDING,
            0,
            Instant.now(),
            null
        );
    }
}
