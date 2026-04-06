package dev.junyoung.exchange.orderservice.domain.model.enums;

public enum OrderStatus {
	/**
	 * 주문 접수
	 */
	PENDING,

	/**
	 * 엔진
	 */
	NEW,
	PARTIALLY_FILLED,
	FILLED,
	CANCELED,
	CANCEL_PENDING,
	REJECTED
}
