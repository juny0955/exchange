package dev.junyoung.exchange.orderservice.domain.model.entity;

import java.time.Instant;

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
	 * <p>
	 *     {@link OutboxStatus#SUCCESS} 상태로 변경한다.
	 *     이미 {@link OutboxStatus#SUCCESS}인 경우 false 리턴
	 * </p>
	 * @return 변경 여부
	 */
    public boolean complete(Instant publishedAt) {
		if (OutboxStatus.SUCCESS.equals(status))
			return false;

        status = OutboxStatus.SUCCESS;
		this.publishedAt = publishedAt;
		return true;
    }
}