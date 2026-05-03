package dev.junyoung.exchange.orderservice.application.service.account;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.junyoung.exchange.orderservice.application.exception.OrderNotFoundException;
import dev.junyoung.exchange.orderservice.application.port.in.account.HandleAccountRejectedEvent;
import dev.junyoung.exchange.orderservice.application.port.out.OrderHistoryRepository;
import dev.junyoung.exchange.orderservice.application.port.out.OrderRepository;
import dev.junyoung.exchange.orderservice.domain.model.entity.Order;
import dev.junyoung.exchange.orderservice.domain.model.entity.OrderHistory;
import dev.junyoung.exchange.orderservice.domain.model.enums.AccountRejectReason;
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
public class AccountRejectedEventHandler implements HandleAccountRejectedEvent {

	private final OrderRepository orderRepository;
	private final OrderHistoryRepository orderHistoryRepository;

	@Override
	public void handle(OrderId orderId, AccountId accountId, AccountRejectReason reason) {
		Order order = orderRepository.findByIdAndAccountIdForUpdate(orderId, accountId)
			.orElseThrow(OrderNotFoundException::new);

		OrderStatus fromStatus = order.getStatus();

		// PENDING 상태 제외 모두 no-op 처리
		if (fromStatus.equals(OrderStatus.PENDING)) {
			order.reject();

			orderRepository.updateStatus(order);
			orderHistoryRepository.save(OrderHistory.createTransition(order, fromStatus, OrderHisReason.ACCOUNT_REJECTED, reason.name()));
			return;
		}

		if (fromStatus.equals(OrderStatus.REJECTED)) {
			log.warn("[ACCOUNT_REJECTED: ignored] 이미 거부된 주문입니다. orderId={}, accountId={}",
				orderId.value(), accountId.value());
			return;
		}

		log.debug("[ACCOUNT_REJECTED: duplicate] 이미 예약 완료 이후 상태입니다. orderId={}, accountId={}, status={}",
			orderId.value(), accountId.value(), fromStatus);
	}
}
