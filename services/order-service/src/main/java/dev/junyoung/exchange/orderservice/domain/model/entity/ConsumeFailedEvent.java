package dev.junyoung.exchange.orderservice.domain.model.entity;

import dev.junyoung.exchange.orderservice.domain.model.enums.ConsumeFailedEventStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class ConsumeFailedEvent {
    private final Long failedId;
    private final String topic;
    private final int partition;
    private final long offset;
    private final String payload;
    private final String errorMessage;
    private ConsumeFailedEventStatus status;
    private int retryCount;
    private final Instant failedAt;
    private Instant resolvedAt;

    public static ConsumeFailedEvent create(
        String topic,
        int partition,
        long offset,
        String payload,
        String errorMessage
    ) {
        return new ConsumeFailedEvent(
            null,
            topic,
            partition,
            offset,
            payload,
            errorMessage,
            ConsumeFailedEventStatus.PENDING,
            0,
            Instant.now(),
            null
        );
    }
}
