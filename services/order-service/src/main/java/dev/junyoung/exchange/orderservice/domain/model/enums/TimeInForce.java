package dev.junyoung.exchange.orderservice.domain.model.enums;

/**
 * 주문 조건
 */
public enum TimeInForce {
	/**
	 * Good Till Canceled
	 * <p>취소할 때까지 유효</p>
	 */
	GTC,

	/**
	 * Immediate Or Cancel
	 * <p>즉시 체결 후 잔량 취소</p>
	 */
	IOC,

	/**
	 * Fill Or Kill
	 * <p>전량 체결 또는 전부 취소</p>
	 */
	FOK
}
