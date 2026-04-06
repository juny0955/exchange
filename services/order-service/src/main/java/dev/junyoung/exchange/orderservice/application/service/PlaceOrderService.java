package dev.junyoung.exchange.orderservice.application.service;

import dev.junyoung.exchange.core.exception.CoreException;
import dev.junyoung.exchange.orderservice.application.exception.OrderErrorCode;
import dev.junyoung.exchange.orderservice.application.port.out.AcceptedSeqGenerator;
import dev.junyoung.exchange.orderservice.application.port.out.OrderRepository;
import dev.junyoung.exchange.orderservice.domain.model.entity.Order;
import org.springframework.stereotype.Service;

import dev.junyoung.exchange.orderservice.application.port.in.PlaceOrderUseCase;
import dev.junyoung.exchange.orderservice.application.port.in.command.PlaceOrderCommand;
import dev.junyoung.exchange.orderservice.domain.model.value.OrderId;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PlaceOrderService implements PlaceOrderUseCase {

	private final AcceptedSeqGenerator acceptedSeqGenerator;
	private final OrderRepository orderRepository;

	@Override
	public OrderId placeOrder(PlaceOrderCommand command) {
		if (orderRepository.existsByAccountIdAndClientOrderId(command.accountId(), command.clientOrderId()))
			throw new CoreException(OrderErrorCode.DUPLICATE_PLACE_ORDER);

		Order order = Order.of(
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
		return order.orderId();
	}
}
