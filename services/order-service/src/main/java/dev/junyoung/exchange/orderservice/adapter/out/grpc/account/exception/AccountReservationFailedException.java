package dev.junyoung.exchange.orderservice.adapter.out.grpc.account.exception;

import lombok.Getter;

@Getter
public class AccountReservationFailedException extends RuntimeException {
	private final String action;
	private final String orderId;
	private final String accountId;
	// TODO detail 추가 필요

	public AccountReservationFailedException(String action, String orderId, String accountId, Throwable cause) {
		super(cause);
		this.action = action;
		this.orderId = orderId;
		this.accountId = accountId;
	}
}
