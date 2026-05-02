package dev.junyoung.exchange.orderservice.domain.model.enums;

public enum OrderStatus {
	/**
	 * 처리 대기 (엔진 진입 전)
	 */
	PENDING,

	/**
	 * 잔고 검증 및 홀딩 완료
	 */
	RESERVED,

	/**
	 * 활성화 (엔진 진입)
	 */
	NEW,

	/**
	 * 부분 체결
	 */
	PARTIALLY_FILLED,

	/**
	 * 부분 체결 (취소 대기중)
	 */
	PARTIALLY_FILLED_CANCEL_PENDING,

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
