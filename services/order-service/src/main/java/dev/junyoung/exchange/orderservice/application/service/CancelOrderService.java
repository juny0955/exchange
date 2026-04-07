package dev.junyoung.exchange.orderservice.application.service;

import dev.junyoung.exchange.core.exception.CoreException;
import dev.junyoung.exchange.orderservice.application.exception.OrderErrorCode;
import dev.junyoung.exchange.orderservice.application.port.in.CancelOrderUseCase;
import dev.junyoung.exchange.orderservice.application.port.in.command.CancelOrderCommand;
import dev.junyoung.exchange.orderservice.application.port.out.OrderExecutionPort;
import dev.junyoung.exchange.orderservice.application.port.out.OrderRepository;
import dev.junyoung.exchange.orderservice.domain.model.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CancelOrderService implements CancelOrderUseCase {

    private final OrderRepository orderRepository;
    private final OrderExecutionPort orderExecutionPort;

    @Override
    public void cancelOrder(CancelOrderCommand command) {
        Order order = orderRepository.findByIdAndAccountIdForUpdate(command.orderId(), command.accountId())
            .orElseThrow(() -> new CoreException(OrderErrorCode.ORDER_NOT_FOUND));

        // TODO PENDING 상태일때 어떻게 처리할지 결정 해야함
        order.requestCancel();

        orderRepository.updateStatus(order);
        orderExecutionPort.cancel(order);
    }
}
