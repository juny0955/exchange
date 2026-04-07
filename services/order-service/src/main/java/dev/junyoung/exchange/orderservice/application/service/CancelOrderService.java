package dev.junyoung.exchange.orderservice.application.service;

import dev.junyoung.exchange.core.exception.CoreException;
import dev.junyoung.exchange.orderservice.application.exception.OrderErrorCode;
import dev.junyoung.exchange.orderservice.application.port.in.CancelOrderUseCase;
import dev.junyoung.exchange.orderservice.application.port.in.command.CancelOrderCommand;
import dev.junyoung.exchange.orderservice.application.port.out.OrderRepository;
import dev.junyoung.exchange.orderservice.domain.model.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CancelOrderService implements CancelOrderUseCase {

    private final OrderRepository orderRepository;

    @Override
    public void cancelOrder(CancelOrderCommand command) {
        Order order = orderRepository.findByIdAndAccountId(command.orderId(), command.accountId())
            .orElseThrow(() -> new CoreException(OrderErrorCode.ORDER_NOT_FOUND));

        order.requestCancel();

        orderRepository.save(order);

        // TODO 엔진 Submit
    }
}
