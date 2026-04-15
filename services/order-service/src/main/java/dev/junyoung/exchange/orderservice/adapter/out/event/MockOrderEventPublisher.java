package dev.junyoung.exchange.orderservice.adapter.out.event;

import dev.junyoung.exchange.orderservice.application.port.out.OrderEventPublisher;
import dev.junyoung.exchange.orderservice.application.port.out.command.CancelOrderEvent;
import dev.junyoung.exchange.orderservice.application.port.out.command.PlaceOrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MockOrderEventPublisher implements OrderEventPublisher {

    @Override
    public void placeOrder(PlaceOrderEvent event) {
        log.info("Order placed: {}", event);
    }

    @Override
    public void cancelOrder(CancelOrderEvent event) {
        log.info("Order cancelled: {}", event);
    }
}
