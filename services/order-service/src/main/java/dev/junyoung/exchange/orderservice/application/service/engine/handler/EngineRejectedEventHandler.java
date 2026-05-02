package dev.junyoung.exchange.orderservice.application.service.engine.handler;

import dev.junyoung.exchange.orderservice.application.service.engine.processor.EngineOrderStateTransitionProcessor;
import dev.junyoung.exchange.orderservice.application.port.in.engine.HandleEngineRejectedEventUseCase;
import dev.junyoung.exchange.orderservice.domain.model.entity.Order;
import dev.junyoung.exchange.orderservice.domain.model.enums.OrderHisReason;
import dev.junyoung.exchange.orderservice.domain.model.enums.RejectReason;
import dev.junyoung.exchange.orderservice.domain.model.value.AccountId;
import dev.junyoung.exchange.orderservice.domain.model.value.OrderId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EngineRejectedEventHandler implements HandleEngineRejectedEventUseCase {

	private final EngineOrderStateTransitionProcessor processor;

	@Override
	public void handle(OrderId orderId, AccountId accountId, RejectReason reason) {
		processor.process(orderId, accountId, Order::reject, OrderHisReason.ENGINE_REJECTED, reason.name());
	}
}
