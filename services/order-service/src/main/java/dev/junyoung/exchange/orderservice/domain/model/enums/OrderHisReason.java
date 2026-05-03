package dev.junyoung.exchange.orderservice.domain.model.enums;

public enum OrderHisReason {
	INIT_ORDER,
	USER_REQUEST_CANCEL,

	ACCOUNT_RESERVED,
	ACCOUNT_REJECTED,

	ENGINE_ACCEPTED,
	ENGINE_REJECTED,
	ENGINE_MATCHED,
	ENGINE_CANCELED,
}
