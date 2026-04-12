package dev.junyoung.exchange.orderservice.adapter.out.grpc.account;

import java.time.Duration;
import java.util.concurrent.locks.LockSupport;

import org.springframework.stereotype.Component;

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
    private static final int MAX_RETRIES = 3;

    private final AccountReservationServiceGrpc.AccountReservationServiceBlockingStub stub;

    @Override
    public void reserve(AccountReserveCommand command) {
        ReserveRequest request = ReserveRequest.newBuilder()
            .setOrderId(command.orderId().toString())
            .setAccountId(command.accountId().toString())
            .setAsset(command.asset())
            .setAmount(command.amount().toPlainString())
            .build();

        retryOnUnavailable(
            () -> stub.withDeadlineAfter(RESERVE_TIMEOUT).reserve(request),
            request.getOrderId(),
            request.getAccountId(),
            "reserve"
        );
    }

    @Override
    public void release(AccountReleaseCommand command) {
        ReleaseRequest request = ReleaseRequest.newBuilder()
            .setAccountId(command.accountId().toString())
            .setOrderId(command.orderId().toString())
            .build();

        retryOnUnavailable(
            () -> stub.withDeadlineAfter(RELEASE_TIMEOUT).release(request),
            request.getOrderId(),
            request.getAccountId(),
            "release"
        );
    }

    private void retryOnUnavailable(
        Runnable runnable,
        String orderId,
        String accountId,
        String action
    ) {
        for (int i=1; i<=MAX_RETRIES; i++) {
            try {
                runnable.run();
                return;
            } catch (StatusRuntimeException e) {
                Status.Code code = e.getStatus().getCode();
                boolean isLastAttempt = i == MAX_RETRIES;

                if (code != Status.Code.UNAVAILABLE) {
                    log.error("gRPC account-service [{}] FAILED: orderId={}, accountId={}, code={}", action, orderId, accountId, code, e);
                    throw e;
                }

                if (isLastAttempt) {
                    log.error("gRPC account-service [{}] FAILED AFTER MAX RETRY: orderId={}, accountId={}, code={}", action, orderId, accountId, code, e);
                    throw e;
                }

                log.warn("gRPC account-service [{}] FAILED: orderId={}, accountId={}, code={}, retrying...", action, orderId, accountId, code);
                LockSupport.parkNanos(Duration.ofMillis(100L * (1L << i)).toNanos());
            }
        }
    }
}
