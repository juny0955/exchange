package dev.junyoung.exchange.orderservice.application.service.tx;

import dev.junyoung.exchange.core.exception.CoreException;
import dev.junyoung.exchange.orderservice.application.exception.OrderErrorCode;
import dev.junyoung.exchange.orderservice.application.port.in.command.CancelOrderCommand;
import dev.junyoung.exchange.orderservice.application.port.out.OrderHistoryRepository;
import dev.junyoung.exchange.orderservice.application.port.out.OrderRepository;
import dev.junyoung.exchange.orderservice.domain.model.entity.Order;
import dev.junyoung.exchange.orderservice.domain.model.entity.OrderHistory;
import dev.junyoung.exchange.orderservice.domain.model.enums.OrderHisReason;
import dev.junyoung.exchange.orderservice.domain.model.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CancelOrderTx {

    private final OrderRepository orderRepository;
    private final OrderHistoryRepository orderHistoryRepository;

    public Order cancelOrderTx(CancelOrderCommand command) {
        Order order = orderRepository.findByIdAndAccountIdForUpdate(command.orderId(), command.accountId())
            .orElseThrow(() -> new CoreException(OrderErrorCode.ORDER_NOT_FOUND));

        OrderStatus fromStatus = order.getStatus();
        // TODO PENDING 상태일때 어떻게 처리할지 결정 해야함
        order.requestCancel();

        orderRepository.updateStatus(order);
        orderHistoryRepository.save(OrderHistory.createTransition(order, fromStatus, OrderHisReason.USER_REQUEST_CANCEL));
        return order;
    }
}
