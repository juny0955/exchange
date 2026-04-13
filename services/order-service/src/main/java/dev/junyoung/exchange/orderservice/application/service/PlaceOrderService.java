package dev.junyoung.exchange.orderservice.application.service;

import org.springframework.stereotype.Service;

import dev.junyoung.exchange.core.exception.CoreException;
import dev.junyoung.exchange.orderservice.adapter.out.grpc.account.exception.AccountReservationFailedException;
import dev.junyoung.exchange.orderservice.application.exception.OrderErrorCode;
import dev.junyoung.exchange.orderservice.application.port.in.PlaceOrderUseCase;
import dev.junyoung.exchange.orderservice.application.port.in.command.PlaceOrderCommand;
import dev.junyoung.exchange.orderservice.application.port.out.AccountReservationPort;
import dev.junyoung.exchange.orderservice.application.port.out.command.AccountReserveCommand;
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

	@Override
	public OrderId placeOrder(PlaceOrderCommand command) {
		Order order = placeOrderTx.persistPendingOrder(command);
		processAccountReserve(order);
		placeOrderTx.saveOutbox(order); // TODO outbox 저장 실패하면?
		return order.getOrderId();
	}

	/**
	 * 잔고 검증 / 홀드를 진행한다
	 *
	 * <p>
	 *     실패시 주문 상태를 REJECTED로 변경 후 예외 응답
	 * </p>
	 *
	 * @param order 해당 주문
	 */
	private void processAccountReserve(Order order) {
		try {
			accountReservationPort.reserve(AccountReserveCommand.from(order));
		} catch (AccountReservationFailedException e) {
			placeOrderTx.rejectOrder(order, OrderHisReason.ACCOUNT_RESERVE_FAILED); // TODO detail 추가 필요
			throw new CoreException(OrderErrorCode.ACCOUNT_RESERVE_FAILED);
		}
	}
}
