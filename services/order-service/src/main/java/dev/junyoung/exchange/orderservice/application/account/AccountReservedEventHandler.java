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
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
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

		// PENDING 상태 제외 모두 no-op 처리
		if (fromStatus.equals(OrderStatus.PENDING)) {
			order.reserved();

			orderRepository.updateStatus(order);
			orderHistoryRepository.save(OrderHistory.createTransition(order, fromStatus, OrderHisReason.ACCOUNT_RESERVED));
			orderOutboxRepository.save(orderOutboxFactory.placeOrder(order));
			return;
		}

		if (fromStatus.equals(OrderStatus.REJECTED)) {
			log.warn("[ACCOUNT_RESERVED: ignored] 이미 거부된 주문입니다. orderId={}, accountId={}",
				orderId.value(), accountId.value());
			return;
		}

		log.debug("[ACCOUNT_RESERVED: duplicate] 이미 예약 완료 이후 상태입니다. orderId={}, accountId={}, status={}",
			orderId.value(), accountId.value(), fromStatus);
	}
}
