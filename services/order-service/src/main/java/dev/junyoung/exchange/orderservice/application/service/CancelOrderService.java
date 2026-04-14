package dev.junyoung.exchange.orderservice.application.service;

import dev.junyoung.exchange.core.exception.CoreException;
import dev.junyoung.exchange.orderservice.application.exception.OrderErrorCode;
import dev.junyoung.exchange.orderservice.application.port.out.OrderHistoryRepository;
import dev.junyoung.exchange.orderservice.application.port.out.OrderOutboxRepository;
import dev.junyoung.exchange.orderservice.application.port.out.OrderRepository;
import dev.junyoung.exchange.orderservice.application.service.outbox.OrderOutboxFactory;
import dev.junyoung.exchange.orderservice.domain.model.entity.OrderHistory;
import dev.junyoung.exchange.orderservice.domain.model.enums.OrderHisReason;
import dev.junyoung.exchange.orderservice.domain.model.enums.OrderStatus;
import org.springframework.stereotype.Service;

import dev.junyoung.exchange.orderservice.application.port.in.CancelOrderUseCase;
import dev.junyoung.exchange.orderservice.application.port.in.command.CancelOrderCommand;
import dev.junyoung.exchange.orderservice.domain.model.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CancelOrderService implements CancelOrderUseCase {

    private final OrderRepository orderRepository;
    private final OrderHistoryRepository orderHistoryRepository;
    private final OrderOutboxRepository orderOutboxRepository;
    private final OrderOutboxFactory orderOutboxFactory;

    @Override
    public void cancelOrder(CancelOrderCommand command) {
        Order order = orderRepository.findByIdAndAccountIdForUpdate(command.orderId(), command.accountId())
            .orElseThrow(() -> new CoreException(OrderErrorCode.ORDER_NOT_FOUND));

        OrderStatus fromStatus = order.getStatus();
        order.requestCancel();

        orderRepository.updateStatus(order);
        orderHistoryRepository.save(OrderHistory.createTransition(order, fromStatus, OrderHisReason.USER_REQUEST_CANCEL));
        orderOutboxRepository.save(orderOutboxFactory.cancelOrder(order));
    }
}
