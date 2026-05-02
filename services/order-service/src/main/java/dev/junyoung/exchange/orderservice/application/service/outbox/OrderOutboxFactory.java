package dev.junyoung.exchange.orderservice.application.service.outbox;

import org.springframework.stereotype.Component;

import dev.junyoung.exchange.orderservice.application.port.out.command.CancelOrderEvent;
import dev.junyoung.exchange.orderservice.application.port.out.command.PlaceOrderEvent;
import dev.junyoung.exchange.orderservice.application.port.out.command.ReserveOrderEvent;
import dev.junyoung.exchange.orderservice.domain.model.entity.Order;
import dev.junyoung.exchange.orderservice.domain.model.entity.OrderOutbox;
import dev.junyoung.exchange.orderservice.domain.model.enums.EventType;
import dev.junyoung.exchange.orderservice.domain.service.dto.AssetReserveResult;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class OrderOutboxFactory {

	private final ObjectMapper objectMapper;

	public OrderOutbox reserveOrder(Order order, AssetReserveResult assetReserveResult) {
		ReserveOrderEvent event = ReserveOrderEvent.of(order, assetReserveResult);
		String payload = objectMapper.writeValueAsString(event);
		String partitionKey = order.getAccountId().value().toString() + ":" + assetReserveResult.asset();
		return OrderOutbox.create(order.getOrderId(), EventType.RESERVE_ORDER, partitionKey, payload);
	}

	public OrderOutbox placeOrder(Order order) {
		PlaceOrderEvent event = PlaceOrderEvent.from(order);
		String payload = objectMapper.writeValueAsString(event);
		String partitionKey = order.getSymbol().getTicker();
		return OrderOutbox.create(order.getOrderId(), EventType.PLACE_ORDER, partitionKey, payload);
	}

    public OrderOutbox cancelOrder(Order order) {
		CancelOrderEvent event = CancelOrderEvent.from(order);
		String payload = objectMapper.writeValueAsString(event);
		String partitionKey = order.getSymbol().getTicker();
		return OrderOutbox.create(order.getOrderId(), EventType.CANCEL_ORDER, partitionKey, payload);
	}
}
