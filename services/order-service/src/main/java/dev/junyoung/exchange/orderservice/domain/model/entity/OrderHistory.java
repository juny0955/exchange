package dev.junyoung.exchange.orderservice.domain.model.entity;

import java.time.Instant;

import dev.junyoung.exchange.orderservice.domain.model.enums.OrderHisReason;
import dev.junyoung.exchange.orderservice.domain.model.enums.OrderStatus;
import dev.junyoung.exchange.orderservice.domain.model.value.OrderId;

/**
 * 주문 상태 변경 추적용
 */
public record OrderHistory(
	Long orderHistoryId,
	OrderId orderId,
	OrderStatus fromStatus,
	OrderStatus toStatus,
	OrderHisReason reason,
	String detail,
	Instant createdAt
) {
	/**
	 * 초기 Order 접수용
	 * @param orderId 주문 ID
	 * @return OrderHistory
	 */
	public static OrderHistory init(OrderId orderId) {
		return new OrderHistory(
			null,
			orderId,
			null,
			OrderStatus.PENDING,
			null,
			null,
			Instant.now()
		);
	}

	/**
	 * 주문 상태 변경 시
	 * @param order 현재 주문
	 * @param fromStatus 기존 상태
	 * @param reason 사유
	 * @return OrderHistory
	 */
	public static OrderHistory createTransition(Order order, OrderStatus fromStatus, OrderHisReason reason) {
		return new OrderHistory(
			null,
			order.getOrderId(),
			fromStatus,
			order.getStatus(),
			reason,
			null,
			Instant.now()
		);
	}
}
