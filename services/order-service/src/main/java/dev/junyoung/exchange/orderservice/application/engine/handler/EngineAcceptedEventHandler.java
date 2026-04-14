package dev.junyoung.exchange.orderservice.application.engine.handler;

import dev.junyoung.exchange.orderservice.application.engine.processor.EngineOrderStateTransitionProcessor;
import dev.junyoung.exchange.orderservice.application.port.in.engine.HandleEngineAcceptedEventUseCase;
import dev.junyoung.exchange.orderservice.domain.model.entity.Order;
import dev.junyoung.exchange.orderservice.domain.model.enums.OrderHisReason;
import dev.junyoung.exchange.orderservice.domain.model.value.AccountId;
import dev.junyoung.exchange.orderservice.domain.model.value.OrderId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EngineAcceptedEventHandler implements HandleEngineAcceptedEventUseCase {

	private final EngineOrderStateTransitionProcessor processor;

	@Override
	public void handle(OrderId orderId, AccountId accountId) {
		processor.process(orderId, accountId, Order::accepted, OrderHisReason.ENGINE_ACCEPTED);
	}
}
