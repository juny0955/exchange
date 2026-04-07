package dev.junyoung.exchange.orderservice.application.service;

import dev.junyoung.exchange.orderservice.application.port.in.CancelOrderUseCase;
import dev.junyoung.exchange.orderservice.application.port.in.command.CancelOrderCommand;
import dev.junyoung.exchange.orderservice.application.port.out.OrderExecutionPort;
import dev.junyoung.exchange.orderservice.application.service.tx.CancelOrderTx;
import dev.junyoung.exchange.orderservice.domain.model.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CancelOrderService implements CancelOrderUseCase {

    private final CancelOrderTx cancelOrderTx;
    private final OrderExecutionPort orderExecutionPort;

    @Override
    public void cancelOrder(CancelOrderCommand command) {
        Order order = cancelOrderTx.cancelOrderTx(command);
        orderExecutionPort.cancel(order);
    }
}
