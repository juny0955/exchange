package dev.junyoung.exchange.orderservice.application.account;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.junyoung.exchange.orderservice.application.exception.OrderNotFoundException;
import dev.junyoung.exchange.orderservice.application.port.in.account.HandleAccountReservedEvent;
import dev.junyoung.exchange.orderservice.application.port.out.OrderHistoryRepository;
import dev.junyoung.exchange.orderservice.application.port.out.OrderOutboxRepository;
import dev.junyoung.exchange.orderservice.application.port.out.OrderRepository;
import dev.junyoung.exchange.orderservice.application.service.outbox.OrderOutboxFactory;
import dev.junyoung.exchange.orderservice.domain.model.entity.Order;
import dev.junyoung.exchange.orderservice.domain.model.entity.OrderHistory;
import dev.junyoung.exchange.orderservice.domain.model.enums.OrderHisReason;
import dev.junyoung.exchange.orderservice.domain.model.enums.OrderStatus;
import dev.junyoung.exchange.orderservice.domain.model.value.AccountId;
import dev.junyoung.exchange.orderservice.domain.model.value.OrderId;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountReservedEventHandler implements HandleAccountReservedEvent {

	private final OrderRepository orderRepository;
	private final OrderHistoryRepository orderHistoryRepository;
	private final OrderOutboxRepository orderOutboxRepository;
	private final OrderOutboxFactory orderOutboxFactory;

	@Override
	public void handle(OrderId orderId, AccountId accountId) {
		Order order = orderRepository.findByIdAndAccountIdForUpdate(orderId, accountId)
			.orElseThrow(OrderNotFoundException::new);

		OrderStatus fromStatus = order.getStatus();

		// 이미 RESERVE 상태일시 no-op 처리
		if (fromStatus.equals(OrderStatus.RESERVED))
			return;

		order.reserved();

		orderRepository.updateStatus(order);
		orderHistoryRepository.save(OrderHistory.createTransition(order, fromStatus, OrderHisReason.ACCOUNT_RESERVED));
		orderOutboxRepository.save(orderOutboxFactory.placeOrder(order));
	}
}
