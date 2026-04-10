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
    public void placeOrder(PlaceOrderEvent command) {
        log.info("Order placed: {}", command);
    }

    @Override
    public void cancel(CancelOrderEvent command) {
        log.info("Order cancelled: {}", command);
    }
}
