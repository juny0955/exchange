package dev.junyoung.exchange.orderservice.application.port.out;

import dev.junyoung.exchange.orderservice.domain.model.entity.Order;

public interface OrderExecutionPort {
    void place(Order order);
    void cancel(Order order);
}
