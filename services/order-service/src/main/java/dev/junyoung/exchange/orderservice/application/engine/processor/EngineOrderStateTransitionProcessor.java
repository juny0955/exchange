package dev.junyoung.exchange.orderservice.application.engine.processor;

import dev.junyoung.exchange.core.exception.CoreException;
import dev.junyoung.exchange.orderservice.application.exception.OrderErrorCode;
import dev.junyoung.exchange.orderservice.application.port.out.OrderHistoryRepository;
import dev.junyoung.exchange.orderservice.application.port.out.OrderRepository;
import dev.junyoung.exchange.orderservice.domain.model.entity.Order;
import dev.junyoung.exchange.orderservice.domain.model.entity.OrderHistory;
import dev.junyoung.exchange.orderservice.domain.model.enums.OrderHisReason;
import dev.junyoung.exchange.orderservice.domain.model.enums.OrderStatus;
import dev.junyoung.exchange.orderservice.domain.model.value.AccountId;
import dev.junyoung.exchange.orderservice.domain.model.value.OrderId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@Transactional
public class EngineOrderStateTransitionProcessor {

    private final OrderRepository orderRepository;
    private final OrderHistoryRepository orderHistoryRepository;

    public void process(
        OrderId orderId,
        AccountId accountId,
        Consumer<Order> transition,
        OrderHisReason reason
    ) {
        Order order = orderRepository.findByIdAndAccountIdForUpdate(orderId, accountId)
            .orElseThrow(() -> new CoreException(OrderErrorCode.ORDER_NOT_FOUND));

        OrderStatus fromStatus = order.getStatus();

        transition.accept(order);

        orderRepository.updateStatus(order);
        orderHistoryRepository.save(OrderHistory.createTransition(order, fromStatus, reason));
    }
}
