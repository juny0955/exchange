package dev.junyoung.exchange.orderservice.domain.model.enums;

public enum OrderHisReason {
	INIT_ORDER,
	USER_REQUEST_CANCEL,

	ENGINE_ACCEPTED,
	ENGINE_REJECTED,
	ENGINE_MATCHED,
	ENGINE_CANCELED,
}
