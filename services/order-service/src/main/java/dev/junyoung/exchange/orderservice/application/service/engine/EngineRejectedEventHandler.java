package dev.junyoung.exchange.orderservice.application.service.engine;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.junyoung.exchange.orderservice.application.exception.OrderNotFoundException;
import dev.junyoung.exchange.orderservice.application.port.in.engine.HandleEngineRejectedEventUseCase;
import dev.junyoung.exchange.orderservice.application.port.out.OrderHistoryRepository;
import dev.junyoung.exchange.orderservice.application.port.out.OrderRepository;
import dev.junyoung.exchange.orderservice.domain.model.entity.Order;
import dev.junyoung.exchange.orderservice.domain.model.entity.OrderHistory;
import dev.junyoung.exchange.orderservice.domain.model.enums.OrderHisReason;
import dev.junyoung.exchange.orderservice.domain.model.enums.OrderStatus;
import dev.junyoung.exchange.orderservice.domain.model.enums.RejectReason;
import dev.junyoung.exchange.orderservice.domain.model.value.AccountId;
import dev.junyoung.exchange.orderservice.domain.model.value.OrderId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class EngineRejectedEventHandler implements HandleEngineRejectedEventUseCase {

	private final OrderRepository orderRepository;
	private final OrderHistoryRepository orderHistoryRepository;

	@Override
	public void handle(OrderId orderId, AccountId accountId, RejectReason reason) {
		Order order = orderRepository.findByIdAndAccountIdForUpdate(orderId, accountId)
			.orElseThrow(OrderNotFoundException::new);

		OrderStatus fromStatus = order.getStatus();

		// RESERVED 상태 제외 모두 no-op 처리
		if (fromStatus.equals(OrderStatus.RESERVED)) {
			order.reject();

			orderRepository.updateStatus(order);
			orderHistoryRepository.save(OrderHistory.createTransition(order, fromStatus, OrderHisReason.ENGINE_REJECTED, reason.name()));
			return;
		}

		if (fromStatus.equals(OrderStatus.REJECTED)) {
			log.debug("[ENGINE_REJECTED: duplicate] 이미 거부된 주문입니다. orderId={}, accountId={}",
				orderId.value(), accountId.value());
			return;
		}

		if (fromStatus.equals(OrderStatus.PENDING)) {
			log.warn("[ENGINE_REJECTED: ignored] 거부할 수 없는 상태입니다. orderId={}, accountId={}, status={}",
				orderId.value(), accountId.value(), fromStatus);
			return;
		}

		log.debug("[ENGINE_REJECTED: duplicate] 이미 활성화 이후 단계로 진입한 주문입니다. orderId={}, accountId={}, status={}",
			orderId.value(), accountId.value(), fromStatus);
	}
}
