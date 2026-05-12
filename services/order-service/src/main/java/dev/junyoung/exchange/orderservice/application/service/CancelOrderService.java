package dev.junyoung.exchange.orderservice.application.service;

import dev.junyoung.exchange.orderservice.application.exception.OrderNotFoundException;
import dev.junyoung.exchange.orderservice.application.port.in.CancelOrderUseCase;
import dev.junyoung.exchange.orderservice.application.port.in.command.CancelOrderCommand;
import dev.junyoung.exchange.orderservice.application.port.out.OrderHistoryRepository;
import dev.junyoung.exchange.orderservice.application.port.out.OrderOutboxRepository;
import dev.junyoung.exchange.orderservice.application.port.out.OrderRepository;
import dev.junyoung.exchange.orderservice.application.service.outbox.OrderOutboxFactory;
import dev.junyoung.exchange.orderservice.domain.model.entity.Order;
import dev.junyoung.exchange.orderservice.domain.model.entity.OrderHistory;
import dev.junyoung.exchange.orderservice.domain.model.enums.OrderHisReason;
import dev.junyoung.exchange.orderservice.domain.model.enums.OrderStatus;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CancelOrderService implements CancelOrderUseCase {

    private final OrderRepository orderRepository;
    private final OrderHistoryRepository orderHistoryRepository;
    private final OrderOutboxRepository orderOutboxRepository;
    private final OrderOutboxFactory orderOutboxFactory;

    @Override
    @NewSpan("order.cancel")
    public void cancelOrder(CancelOrderCommand command) {
        Order order = orderRepository.findByIdAndAccountIdForUpdate(command.orderId(), command.accountId())
            .orElseThrow(OrderNotFoundException::new);

        OrderStatus fromStatus = order.getStatus();
        order.requestCancel();

        orderRepository.updateStatus(order);
        orderHistoryRepository.save(OrderHistory.createTransition(order, fromStatus, OrderHisReason.USER_REQUEST_CANCEL));
        orderOutboxRepository.save(orderOutboxFactory.cancelOrder(order));

        log.info("[CANCEL_ORDER] 주문 취소 요청 완료. orderId={}, fromStatus={}",
            order.getOrderId().value(), fromStatus);
    }
}
