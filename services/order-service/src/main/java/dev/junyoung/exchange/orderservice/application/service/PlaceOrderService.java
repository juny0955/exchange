package dev.junyoung.exchange.orderservice.application.service;

import dev.junyoung.exchange.orderservice.application.port.in.PlaceOrderUseCase;
import dev.junyoung.exchange.orderservice.application.port.in.command.PlaceOrderCommand;
import dev.junyoung.exchange.orderservice.application.port.out.OrderExecutionPort;
import dev.junyoung.exchange.orderservice.application.service.tx.PlaceOrderTx;
import dev.junyoung.exchange.orderservice.domain.model.entity.Order;
import dev.junyoung.exchange.orderservice.domain.model.value.OrderId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlaceOrderService implements PlaceOrderUseCase {

	private final PlaceOrderTx placeOrderTx;
	private final OrderExecutionPort orderExecutionPort;

	@Override
	public OrderId placeOrder(PlaceOrderCommand command) {
		// TODO account 잔고 검증/홀드 등
		Order order = placeOrderTx.placeOrderTx(command);
		orderExecutionPort.place(order);
		return order.getOrderId();
	}
}
