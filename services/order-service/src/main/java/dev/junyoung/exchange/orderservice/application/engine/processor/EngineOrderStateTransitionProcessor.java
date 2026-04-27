package dev.junyoung.exchange.orderservice.application.engine.processor;

import dev.junyoung.exchange.orderservice.application.exception.OrderNotFoundException;
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
        OrderHisReason reason,
        String detail
    ) {
        Order order = orderRepository.findByIdAndAccountIdForUpdate(orderId, accountId)
            .orElseThrow(OrderNotFoundException::new);

        OrderStatus fromStatus = order.getStatus();

        transition.accept(order);

        orderRepository.updateStatus(order);
        orderHistoryRepository.save(OrderHistory.createTransition(order, fromStatus, reason, detail));
    }
}
