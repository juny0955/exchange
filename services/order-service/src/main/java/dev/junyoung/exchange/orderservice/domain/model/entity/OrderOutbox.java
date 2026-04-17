package dev.junyoung.exchange.orderservice.domain.model.entity;

import java.time.Instant;

import dev.junyoung.exchange.orderservice.domain.exception.OrderOutboxAlreadySuccessException;
import dev.junyoung.exchange.orderservice.domain.model.enums.EventType;
import dev.junyoung.exchange.orderservice.domain.model.enums.OutboxStatus;
import dev.junyoung.exchange.orderservice.domain.model.value.OrderId;
import dev.junyoung.exchange.orderservice.domain.model.value.OutboxId;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderOutbox {
	private final OutboxId outboxId;
	private final OrderId orderId;
	private final String aggregateType;
	private final EventType type;
	private final String payload;
	private OutboxStatus status;
	private int retryCount;
	private final Instant createdAt;
	private Instant publishedAt;

	private static final String AGGREGATE_TYPE = "ORDER";

	public static OrderOutbox create(OrderId orderId, EventType type, String payload) {
		return new OrderOutbox(
			OutboxId.newId(),
			orderId,
			AGGREGATE_TYPE,
			type,
			payload,
			OutboxStatus.PENDING,
			0,
			Instant.now(),
			null
		);
	}

	/**
	 * 성공 상태로 변경한다
	 *
	 * <p>{@link OutboxStatus#SUCCESS} 상태로 변경 </p>
	 * @throws OrderOutboxAlreadySuccessException 이미 성공 상태인 경우
	 */
    public void published() {
		if (OutboxStatus.SUCCESS.equals(status))
			throw new OrderOutboxAlreadySuccessException(outboxId);
        status = OutboxStatus.SUCCESS;
		publishedAt = Instant.now();
    }
}