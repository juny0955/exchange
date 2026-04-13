package dev.junyoung.exchange.orderservice.application.service.outbox;

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
}
