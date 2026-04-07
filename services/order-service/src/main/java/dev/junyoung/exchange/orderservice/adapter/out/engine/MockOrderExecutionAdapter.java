package dev.junyoung.exchange.orderservice.adapter.out.engine;

import dev.junyoung.exchange.orderservice.application.port.out.OrderExecutionPort;
import dev.junyoung.exchange.orderservice.domain.model.entity.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

// TODO gRPC 구현체 변경 필요
@Component
@Slf4j
public class MockOrderExecutionAdapter implements OrderExecutionPort {

    @Override
    public void place(Order order) {
        log.info("Order placed: {}", order);
    }

    @Override
    public void cancel(Order order) {
        log.info("Order cancelled: {}", order);
    }
}
