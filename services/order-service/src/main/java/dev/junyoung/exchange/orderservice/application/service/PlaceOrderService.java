package dev.junyoung.exchange.orderservice.application.service;

import dev.junyoung.exchange.orderservice.application.port.in.PlaceOrderUseCase;
import dev.junyoung.exchange.orderservice.application.port.in.command.PlaceOrderCommand;
import dev.junyoung.exchange.orderservice.application.port.out.AccountReservationPort;
import dev.junyoung.exchange.orderservice.application.port.out.EngineExecutionPort;
import dev.junyoung.exchange.orderservice.application.port.out.command.AccountReserveCommand;
import dev.junyoung.exchange.orderservice.application.port.out.command.EnginePlaceCommand;
import dev.junyoung.exchange.orderservice.application.service.tx.PlaceOrderTx;
import dev.junyoung.exchange.orderservice.domain.model.entity.Order;
import dev.junyoung.exchange.orderservice.domain.model.value.OrderId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlaceOrderService implements PlaceOrderUseCase {

	private final AccountReservationPort accountReservationPort;
	private final PlaceOrderTx placeOrderTx;
	private final EngineExecutionPort engineExecutionPort;

	@Override
	public OrderId placeOrder(PlaceOrderCommand command) {
		Order order = placeOrderTx.persistPendingOrder(command);

		// TODO 복구 트랜잭션 추가 필요
		accountReservationPort.reserve(AccountReserveCommand.of(order));
		engineExecutionPort.place(EnginePlaceCommand.of(order));

		return order.getOrderId();
	}
}
