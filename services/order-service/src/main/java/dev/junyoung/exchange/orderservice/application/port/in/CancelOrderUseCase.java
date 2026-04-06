package dev.junyoung.exchange.orderservice.application.port.in;

import dev.junyoung.exchange.orderservice.application.port.in.command.CancelOrderCommand;

public interface CancelOrderUseCase {
    void cancelOrder(CancelOrderCommand command);
}
