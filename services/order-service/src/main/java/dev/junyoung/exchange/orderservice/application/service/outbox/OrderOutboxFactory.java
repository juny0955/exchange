package dev.junyoung.exchange.orderservice.application.service.outbox;

import dev.junyoung.exchange.orderservice.application.port.out.command.CancelOrderEvent;
import org.springframework.stereotype.Component;

import dev.junyoung.exchange.orderservice.application.port.out.command.PlaceOrderEvent;
import dev.junyoung.exchange.orderservice.domain.model.entity.Order;
import dev.junyoung.exchange.orderservice.domain.model.entity.OrderOutbox;
import dev.junyoung.exchange.orderservice.domain.model.enums.EventType;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class OrderOutboxFactory {

	private final ObjectMapper objectMapper;

	public OrderOutbox placeOrder(Order order) {
		PlaceOrderEvent event = PlaceOrderEvent.of(order);
		String payload = objectMapper.writeValueAsString(event);
		return OrderOutbox.create(order.getOrderId(), EventType.PLACE_ORDER, payload);
	}

    public OrderOutbox cancelOrder(Order order) {
		CancelOrderEvent event = new CancelOrderEvent(order.getOrderId().value(), order.getAccountId().value());
		String payload = objectMapper.writeValueAsString(event);
		return OrderOutbox.create(order.getOrderId(), EventType.CANCEL_ORDER, payload);
	}
}
