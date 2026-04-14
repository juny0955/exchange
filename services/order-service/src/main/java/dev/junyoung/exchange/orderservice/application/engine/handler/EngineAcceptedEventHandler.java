package dev.junyoung.exchange.orderservice.application.engine.handler;

import dev.junyoung.exchange.core.exception.CoreException;
import dev.junyoung.exchange.orderservice.application.exception.OrderErrorCode;
import dev.junyoung.exchange.orderservice.application.port.in.engine.HandleEngineAcceptedEventUseCase;
import dev.junyoung.exchange.orderservice.application.port.out.OrderHistoryRepository;
import dev.junyoung.exchange.orderservice.application.port.out.OrderRepository;
import dev.junyoung.exchange.orderservice.domain.model.entity.Order;
import dev.junyoung.exchange.orderservice.domain.model.entity.OrderHistory;
import dev.junyoung.exchange.orderservice.domain.model.enums.OrderHisReason;
import dev.junyoung.exchange.orderservice.domain.model.enums.OrderStatus;
import dev.junyoung.exchange.orderservice.domain.model.value.AccountId;
import dev.junyoung.exchange.orderservice.domain.model.value.OrderId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EngineAcceptedEventHandler implements HandleEngineAcceptedEventUseCase {

	private final OrderRepository orderRepository;
	private final OrderHistoryRepository orderHistoryRepository;

	@Override
	public void handle(OrderId orderId, AccountId accountId) {
		Order order = orderRepository.findByIdAndAccountIdForUpdate(orderId, accountId)
			.orElseThrow(() -> new CoreException(OrderErrorCode.ORDER_NOT_FOUND));

		OrderStatus fromStatus = order.getStatus();

		// 취소 대기 상태일시 상태 변경 하지않고 이력만 남김
		if (!order.isCancelPendingStatus()) {
			order.accepted();
			orderRepository.updateStatus(order);
		}

		orderHistoryRepository.save(OrderHistory.createTransition(order, fromStatus, OrderHisReason.ENGINE_ACCEPTED));
	}
}
