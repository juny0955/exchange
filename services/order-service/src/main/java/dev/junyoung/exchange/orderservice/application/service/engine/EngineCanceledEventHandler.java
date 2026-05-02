package dev.junyoung.exchange.orderservice.application.service.engine;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.junyoung.exchange.orderservice.application.exception.OrderNotFoundException;
import dev.junyoung.exchange.orderservice.application.port.in.engine.HandleEngineCanceledEventUseCase;
import dev.junyoung.exchange.orderservice.application.port.out.OrderHistoryRepository;
import dev.junyoung.exchange.orderservice.application.port.out.OrderRepository;
import dev.junyoung.exchange.orderservice.domain.model.entity.Order;
import dev.junyoung.exchange.orderservice.domain.model.entity.OrderHistory;
import dev.junyoung.exchange.orderservice.domain.model.enums.CancelReason;
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
public class EngineCanceledEventHandler implements HandleEngineCanceledEventUseCase {

	private final OrderRepository orderRepository;
	private final OrderHistoryRepository orderHistoryRepository;

	@Override
	public void handle(OrderId orderId, AccountId accountId, CancelReason reason) {
		Order order = orderRepository.findByIdAndAccountIdForUpdate(orderId, accountId)
			.orElseThrow(OrderNotFoundException::new);

		OrderStatus fromStatus = order.getStatus();
		if (order.isCancelPendingStatus()) {
			order.cancel();

			orderRepository.updateStatus(order);
			orderHistoryRepository.save(OrderHistory.createTransition(order, fromStatus, OrderHisReason.ENGINE_CANCELED, reason.name()));
			return;
		}

		if (fromStatus.equals(OrderStatus.CANCELED)) {
			log.debug("[ENGINE_CANCELED: duplicate] 이미 취소된 주문입니다. orderId={}, accountId={}",
				orderId.value(), accountId.value());
			return;
		}

		log.warn("[ENGINE_CANCELED: ignored] 취소할 수 없는 상태입니다. orderId={}, accountId={}, status={}, reason={}",
			orderId.value(), accountId.value(), fromStatus, reason);
	}
}
