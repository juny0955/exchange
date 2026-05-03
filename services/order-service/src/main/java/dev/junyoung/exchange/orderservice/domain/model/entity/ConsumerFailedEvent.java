package dev.junyoung.exchange.orderservice.domain.model.entity;

import dev.junyoung.exchange.orderservice.domain.model.enums.ConsumerFailedEventStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class ConsumerFailedEvent {
    private final Long failedId;
    private final String topic;
    private final int partition;
    private final long offset;
    private final String payload;
    private final String errorMessage;
    private ConsumerFailedEventStatus status;
    private int retryCount;
    private final Instant failedAt;
    private Instant resolvedAt;

    public static ConsumerFailedEvent create(
        String topic,
        int partition,
        long offset,
        String payload,
        String errorMessage
    ) {
        return new ConsumerFailedEvent(
            null,
            topic,
            partition,
            offset,
            payload,
            errorMessage,
            ConsumerFailedEventStatus.PENDING,
            0,
            Instant.now(),
            null
        );
    }
}
