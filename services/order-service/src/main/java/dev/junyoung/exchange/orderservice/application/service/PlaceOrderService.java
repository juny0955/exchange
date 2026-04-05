package dev.junyoung.exchange.orderservice.application.service;

import org.springframework.stereotype.Service;

import dev.junyoung.exchange.orderservice.application.port.in.PlaceOrderUseCase;
import dev.junyoung.exchange.orderservice.application.port.in.command.PlaceOrderCommand;
import dev.junyoung.exchange.orderservice.domain.model.value.OrderId;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlaceOrderService implements PlaceOrderUseCase {

	@Override
	public OrderId placeOrder(PlaceOrderCommand command) {
		return null;
	}
}
