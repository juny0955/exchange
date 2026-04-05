package dev.junyoung.exchange.orderservice.application.port.in;

import dev.junyoung.exchange.orderservice.application.port.in.command.PlaceOrderCommand;
import dev.junyoung.exchange.orderservice.domain.model.value.OrderId;

public interface PlaceOrderUseCase {
	OrderId placeOrder(PlaceOrderCommand command);
}
