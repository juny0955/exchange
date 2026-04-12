package dev.junyoung.exchange.orderservice.adapter.out.grpc.account;

import java.time.Duration;

import org.springframework.core.retry.RetryException;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.stereotype.Component;

import dev.junyoung.exchange.orderservice.adapter.out.grpc.account.exception.AccountReservationFailedException;
import dev.junyoung.exchange.orderservice.adapter.out.grpc.account.exception.RetryableAccountGrpcException;
import dev.junyoung.exchange.orderservice.application.port.out.AccountReservationPort;
import dev.junyoung.exchange.orderservice.application.port.out.command.AccountReleaseCommand;
import dev.junyoung.exchange.orderservice.application.port.out.command.AccountReserveCommand;
import dev.junyoung.exchange.proto.account.v1.AccountReservationServiceGrpc;
import dev.junyoung.exchange.proto.account.v1.ReleaseRequest;
import dev.junyoung.exchange.proto.account.v1.ReserveRequest;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class GrpcAccountReservationAdapter implements AccountReservationPort {

    private static final Duration RESERVE_TIMEOUT = Duration.ofSeconds(1);
    private static final Duration RELEASE_TIMEOUT = Duration.ofSeconds(2);

    private final AccountReservationServiceGrpc.AccountReservationServiceBlockingStub stub;
	private final RetryTemplate accountGrpcRetryTemplate;

	@Override
    public void reserve(AccountReserveCommand command) {
        ReserveRequest request = ReserveRequest.newBuilder()
			.setOrderId(command.orderId().toString())
			.setAccountId(command.accountId().toString())
			.setAsset(command.asset())
			.setAmount(command.amount().toPlainString())
			.build();

		processWithRetry(
			() -> stub.withDeadlineAfter(RESERVE_TIMEOUT).reserve(request),
			"reserve",
			request.getOrderId(),
			request.getAccountId()
		);
    }

    @Override
    public void release(AccountReleaseCommand command) {
        ReleaseRequest request = ReleaseRequest.newBuilder()
			.setAccountId(command.accountId().toString())
			.setOrderId(command.orderId().toString())
			.build();

		processWithRetry(
			() -> stub.withDeadlineAfter(RELEASE_TIMEOUT).release(request),
			"release",
			request.getOrderId(),
			request.getAccountId()
		);
    }

	private void processWithRetry(
		Runnable runnable,
		String action,
		String orderId,
		String accountId
	) {
		try {
			accountGrpcRetryTemplate.execute(() -> {
				try {
					runnable.run();
					return null;
				} catch (StatusRuntimeException e) {
					throw handleException(action, orderId, accountId, e);
				}
			});
		} catch (RetryException e) {
			Throwable cause = e.getCause();
			log.error("gRPC account-service [{}] failed: orderId={}, accountId={}", action, orderId, accountId, cause);
			if (cause instanceof AccountReservationFailedException ex) throw ex;
			throw new AccountReservationFailedException(action, orderId, accountId, cause);
		}
	}

    private AccountReservationFailedException handleException(
		String action,
		String orderId,
		String accountId,
		StatusRuntimeException e
    ) {
		Status.Code code = e.getStatus().getCode();
		if (code == Status.Code.UNAVAILABLE) {
			log.warn("gRPC [{}] retryable: orderId={}, accountId={}, code={}", action, orderId, accountId, code);
			return new RetryableAccountGrpcException(action, orderId, accountId, e);
		}

		log.error("gRPC account-service [{}] failed: orderId={}, accountId={}", action, orderId, accountId);
		return new AccountReservationFailedException(action, orderId, accountId, e);
    }
}
