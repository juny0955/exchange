package dev.junyoung.exchange.orderservice.adapter.out.grpc.account.exception;

public class RetryableAccountGrpcException extends AccountReservationFailedException {
	public RetryableAccountGrpcException(String action, String orderId, String accountId, Throwable cause) {
		super(action, orderId, accountId, cause);
	}
}
