package dev.junyoung.exchange.orderservice.application.service;

import org.springframework.stereotype.Service;

import dev.junyoung.exchange.orderservice.application.port.in.CancelOrderUseCase;
import dev.junyoung.exchange.orderservice.application.port.in.command.CancelOrderCommand;
import dev.junyoung.exchange.orderservice.application.port.out.OrderEventPublisher;
import dev.junyoung.exchange.orderservice.application.port.out.command.CancelOrderEvent;
import dev.junyoung.exchange.orderservice.application.service.tx.CancelOrderTx;
import dev.junyoung.exchange.orderservice.domain.model.entity.Order;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CancelOrderService implements CancelOrderUseCase {

    private final CancelOrderTx cancelOrderTx;
    private final OrderEventPublisher orderEventPublisher;

    @Override
    public void cancelOrder(CancelOrderCommand command) {
        Order order = cancelOrderTx.cancelOrderTx(command);
        orderEventPublisher.cancel(new CancelOrderEvent(order.getOrderId().value(), order.getAccountId().value()));
    }
}
