package dev.junyoung.exchange.orderservice.application.service.tx;

import dev.junyoung.exchange.core.exception.CoreException;
import dev.junyoung.exchange.orderservice.application.exception.OrderErrorCode;
import dev.junyoung.exchange.orderservice.application.port.in.command.PlaceOrderCommand;
import dev.junyoung.exchange.orderservice.application.port.out.AcceptedSeqGenerator;
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
public class PlaceOrderTx {

    private final AcceptedSeqGenerator acceptedSeqGenerator;
    private final OrderRepository orderRepository;
    private final OrderHistoryRepository orderHistoryRepository;

    public Order persistPendingOrder(PlaceOrderCommand command) {
        if (orderRepository.existsByAccountIdAndClientOrderId(command.accountId(), command.clientOrderId()))
            throw new CoreException(OrderErrorCode.DUPLICATE_PLACE_ORDER);

        Order order = Order.create(
            command.accountId(),
            command.clientOrderId(),
            acceptedSeqGenerator.next(),
            command.symbol(),
            command.side(),
            command.orderType(),
            command.tif(),
            command.price(),
            command.quantity(),
            command.quoteQty(),
            command.orderedAt()
        );

        orderRepository.save(order);
        orderHistoryRepository.save(OrderHistory.init(order.getOrderId()));
        return order;
    }

	public void rejectOrder(Order order, OrderHisReason reason) {
        OrderStatus fromStatus = order.getStatus();
        order.reject();
        orderRepository.updateStatus(order);
        orderHistoryRepository.save(OrderHistory.createTransition(order, fromStatus, reason));
	}
}
