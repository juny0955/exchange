package dev.junyoung.exchange.orderservice.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.junyoung.exchange.orderservice.application.exception.OrderDuplicateException;
import dev.junyoung.exchange.orderservice.application.port.in.PlaceOrderUseCase;
import dev.junyoung.exchange.orderservice.application.port.in.command.PlaceOrderCommand;
import dev.junyoung.exchange.orderservice.application.port.out.AcceptedSeqGenerator;
import dev.junyoung.exchange.orderservice.application.port.out.OrderHistoryRepository;
import dev.junyoung.exchange.orderservice.application.port.out.OrderOutboxRepository;
import dev.junyoung.exchange.orderservice.application.port.out.OrderRepository;
import dev.junyoung.exchange.orderservice.application.service.outbox.OrderOutboxFactory;
import dev.junyoung.exchange.orderservice.domain.model.entity.Order;
import dev.junyoung.exchange.orderservice.domain.model.entity.OrderHistory;
import dev.junyoung.exchange.orderservice.domain.model.value.OrderId;
import dev.junyoung.exchange.orderservice.domain.service.AssetReserveCalculator;
import dev.junyoung.exchange.orderservice.domain.service.dto.AssetReserveResult;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PlaceOrderService implements PlaceOrderUseCase {

	private final AcceptedSeqGenerator acceptedSeqGenerator;
	private final OrderRepository orderRepository;
	private final OrderHistoryRepository orderHistoryRepository;
	private final OrderOutboxFactory orderOutboxFactory;
	private final OrderOutboxRepository orderOutboxRepository;

	@Override
	public OrderId placeOrder(PlaceOrderCommand command) {
		/*
		TODO race condition issue
		 DB uk 제약조건으로 막히긴 하지만 정리할 필요 있음
		 */
		if (orderRepository.existsByAccountIdAndClientOrderId(command.accountId(), command.clientOrderId()))
			throw new OrderDuplicateException();

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

		AssetReserveResult calculate = AssetReserveCalculator.calculate(order);
		orderOutboxRepository.save(orderOutboxFactory.reserveOrder(order, calculate));
		return order.getOrderId();
	}
}
