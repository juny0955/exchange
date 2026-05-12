package dev.junyoung.exchange.orderservice.application.service.engine;

import dev.junyoung.exchange.orderservice.application.exception.OrderNotFoundException;
import dev.junyoung.exchange.orderservice.application.port.in.engine.HandleEngineAcceptedEventUseCase;
import dev.junyoung.exchange.orderservice.application.port.out.OrderHistoryRepository;
import dev.junyoung.exchange.orderservice.application.port.out.OrderRepository;
import dev.junyoung.exchange.orderservice.domain.model.entity.Order;
import dev.junyoung.exchange.orderservice.domain.model.entity.OrderHistory;
import dev.junyoung.exchange.orderservice.domain.model.enums.OrderHisReason;
import dev.junyoung.exchange.orderservice.domain.model.enums.OrderStatus;
import dev.junyoung.exchange.orderservice.domain.model.value.AccountId;
import dev.junyoung.exchange.orderservice.domain.model.value.OrderId;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class EngineAcceptedEventHandler implements HandleEngineAcceptedEventUseCase {

	private final OrderRepository orderRepository;
	private final OrderHistoryRepository orderHistoryRepository;

	@Override
	@NewSpan("order.engine.accepted")
	public void handle(OrderId orderId, AccountId accountId) {
		Order order = orderRepository.findByIdAndAccountIdForUpdate(orderId, accountId)
			.orElseThrow(OrderNotFoundException::new);

		OrderStatus fromStatus = order.getStatus();

		// RESERVED 상태 제외 모두 no-op 처리
		if (fromStatus.equals(OrderStatus.RESERVED)) {
			order.accepted();

			orderRepository.updateStatus(order);
			orderHistoryRepository.save(OrderHistory.createTransition(order, fromStatus, OrderHisReason.ENGINE_ACCEPTED));
			log.info("[ENGINE_ACCEPTED] 주문 엔진 수락 완료. orderId={}", orderId.value());
			return;
		}

		if (fromStatus.equals(OrderStatus.NEW)
			|| fromStatus.equals(OrderStatus.PARTIALLY_FILLED)
			|| order.isFinal()
			|| order.isCancelPendingStatus()) {
			log.debug("[ENGINE_ACCEPTED: duplicate] 이미 활성화된 주문입니다. orderId={}, accountId={}, status={}",
				orderId.value(), accountId.value(), fromStatus);
			return;
		}

		log.warn("[ENGINE_ACCEPTED: ignored] 활성화할 수 없는 상태입니다. orderId={}, accountId={}, status={}",
			orderId.value(), accountId.value(), fromStatus);
	}
}
