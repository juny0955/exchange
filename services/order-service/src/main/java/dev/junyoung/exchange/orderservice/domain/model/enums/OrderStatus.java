package dev.junyoung.exchange.orderservice.domain.model.enums;

public enum OrderStatus {
	/**
	 * 주문 접수
	 */
	PENDING,

	/**
	 * 활성화 (엔진 진입)
	 */
	NEW,

	/**
	 * 부분 체결
	 */
	PARTIALLY_FILLED,

	/**
	 * 전량 체결
	 */
	FILLED,

	/**
	 * 취소 대기
	 */
	CANCEL_PENDING,

	/**
	 * 취소됨
	 */
	CANCELED,

	/**
	 * 거부됨
	 */
	REJECTED
}
