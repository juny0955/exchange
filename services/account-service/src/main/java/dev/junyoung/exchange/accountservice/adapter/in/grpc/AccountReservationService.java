package dev.junyoung.exchange.accountservice.adapter.in.grpc;

import dev.junyoung.exchange.accountservice.application.port.in.ReserveBalanceUseCase;
import dev.junyoung.exchange.accountservice.application.port.in.command.ReserveBalanceCommand;
import dev.junyoung.exchange.accountservice.domain.model.value.AccountId;
import dev.junyoung.exchange.accountservice.domain.model.value.AssetCode;
import dev.junyoung.exchange.accountservice.domain.model.value.OrderId;
import dev.junyoung.exchange.proto.account.v1.*;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.grpc.server.service.GrpcService;

import java.math.BigDecimal;

@GrpcService
@RequiredArgsConstructor
public class AccountReservationService extends AccountReservationServiceGrpc.AccountReservationServiceImplBase {

    private final ReserveBalanceUseCase reserveBalanceUseCase;

    @Override
    public void reserve(ReserveRequest request, StreamObserver<ReserveResponse> responseObserver) {
        ReserveBalanceCommand command = new ReserveBalanceCommand(
            OrderId.from(request.getOrderId()),
            AccountId.from(request.getAccountId()),
            AssetCode.from(request.getAsset()),
            new BigDecimal(request.getAmount())
        );

        reserveBalanceUseCase.reserve(command);
        responseObserver.onNext(ReserveResponse.getDefaultInstance());
        responseObserver.onCompleted();
    }
}
