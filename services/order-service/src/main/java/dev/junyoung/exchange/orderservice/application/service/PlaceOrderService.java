package dev.junyoung.exchange.orderservice.application.service;

import dev.junyoung.exchange.orderservice.application.port.in.PlaceOrderUseCase;
import dev.junyoung.exchange.orderservice.application.port.in.command.PlaceOrderCommand;
import dev.junyoung.exchange.orderservice.application.port.out.AccountReservationPort;
import dev.junyoung.exchange.orderservice.application.port.out.EngineExecutionPort;
import dev.junyoung.exchange.orderservice.application.port.out.command.AccountReleaseCommand;
import dev.junyoung.exchange.orderservice.application.port.out.command.AccountReserveCommand;
import dev.junyoung.exchange.orderservice.application.port.out.command.EnginePlaceCommand;
import dev.junyoung.exchange.orderservice.application.service.tx.PlaceOrderTx;
import dev.junyoung.exchange.orderservice.domain.model.entity.Order;
import dev.junyoung.exchange.orderservice.domain.model.enums.OrderHisReason;
import dev.junyoung.exchange.orderservice.domain.model.value.OrderId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * <h1>주문 접수 서비스</h1>
 *
 * <p>
 *     Order 선저장 (PENDING) -> Account 잔고 검증 / 홀드 -> Matching Engine 주문 접수 순서로 동작
 * </p>
 *
 */
@Service
@RequiredArgsConstructor
public class PlaceOrderService implements PlaceOrderUseCase {

	private final PlaceOrderTx placeOrderTx;
	private final AccountReservationPort accountReservationPort;
	private final EngineExecutionPort engineExecutionPort;

	@Override
	public OrderId placeOrder(PlaceOrderCommand command) {
		Order order = placeOrderTx.persistPendingOrder(command);

		processAccountReserve(order);
		processEngineExecute(order);

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

	/**
	 * 매칭 엔진으로 주문을 접수한다
	 *
	 * @param order 해당 주문
	 */
	private void processEngineExecute(Order order) {
		try {
			engineExecutionPort.place(EnginePlaceCommand.of(order));
		} catch (Exception e) { // TODO 에외 세분화 필요
			placeOrderTx.rejectOrder(order, OrderHisReason.ENGINE_REJECTED); // TODO detail 추가 필요
			accountReservationPort.release(AccountReleaseCommand.from(order));
			throw e;
		}
	}
}
