package dev.junyoung.exchange.orderservice.application.port.out;

import dev.junyoung.exchange.orderservice.application.port.out.command.CancelOrderEvent;
import dev.junyoung.exchange.orderservice.application.port.out.command.PlaceOrderEvent;

public interface OrderEventPublisher {
    void placeOrder(PlaceOrderEvent event);
    void cancelOrder(CancelOrderEvent event);
}
