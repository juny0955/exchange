package dev.junyoung.exchange.orderservice.application.service;

import org.springframework.stereotype.Service;

import dev.junyoung.exchange.orderservice.application.port.in.PlaceOrderUseCase;
import dev.junyoung.exchange.orderservice.application.port.in.command.PlaceOrderCommand;
import dev.junyoung.exchange.orderservice.application.port.out.AccountReservationPort;
import dev.junyoung.exchange.orderservice.application.port.out.OrderEventPublisher;
import dev.junyoung.exchange.orderservice.application.port.out.command.AccountReserveCommand;
import dev.junyoung.exchange.orderservice.application.port.out.command.PlaceOrderEvent;
import dev.junyoung.exchange.orderservice.application.service.tx.PlaceOrderTx;
import dev.junyoung.exchange.orderservice.domain.model.entity.Order;
import dev.junyoung.exchange.orderservice.domain.model.enums.OrderHisReason;
import dev.junyoung.exchange.orderservice.domain.model.value.OrderId;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlaceOrderService implements PlaceOrderUseCase {

	private final PlaceOrderTx placeOrderTx;
	private final AccountReservationPort accountReservationPort;
	private final OrderEventPublisher orderEventPublisher;

	@Override
	public OrderId placeOrder(PlaceOrderCommand command) {
		Order order = placeOrderTx.persistPendingOrder(command);
		processAccountReserve(order);

		orderEventPublisher.placeOrder(PlaceOrderEvent.of(order));
		return order.getOrderId();
	}

	/**
	 * 잔고 검증 / 홀드를 진행한다
	 *
	 * @param order 해당 주문
	 */
	private void processAccountReserve(Order order) {
		try {
			accountReservationPort.reserve(AccountReserveCommand.from(order));
		} catch (Exception e) { // TODO 에외 세분화 필요
			placeOrderTx.rejectOrder(order, OrderHisReason.ACCOUNT_RESERVE_FAILED); // TODO detail 추가 필요
			throw e;
		}
	}
}
