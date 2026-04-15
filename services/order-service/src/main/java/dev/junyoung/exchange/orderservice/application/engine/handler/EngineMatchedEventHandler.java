package dev.junyoung.exchange.orderservice.application.engine.handler;

import dev.junyoung.exchange.orderservice.application.port.in.command.EngineMatchedCommand;
import dev.junyoung.exchange.orderservice.application.port.in.engine.HandleEngineMatchedEventUseCase;
import dev.junyoung.exchange.orderservice.application.port.out.OrderHistoryRepository;
import dev.junyoung.exchange.orderservice.application.port.out.OrderRepository;
import dev.junyoung.exchange.orderservice.application.port.out.TradeRepository;
import dev.junyoung.exchange.orderservice.domain.model.entity.Order;
import dev.junyoung.exchange.orderservice.domain.model.entity.OrderHistory;
import dev.junyoung.exchange.orderservice.domain.model.entity.Trade;
import dev.junyoung.exchange.orderservice.domain.model.enums.OrderHisReason;
import dev.junyoung.exchange.orderservice.domain.model.enums.OrderStatus;
import dev.junyoung.exchange.orderservice.domain.model.value.OrderId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EngineMatchedEventHandler implements HandleEngineMatchedEventUseCase {

	private final OrderRepository orderRepository;
	private final OrderHistoryRepository orderHistoryRepository;
	private final TradeRepository tradeRepository;

	@Override
	public void handle(EngineMatchedCommand command) {
		List<Order> orders = orderRepository.findAllByIdForUpdate(List.of(command.buyOrderId(), command.sellOrderId()));

		Map<OrderId, Order> orderMap = orders.stream()
			.collect(Collectors.toMap(Order::getOrderId, Function.identity()));

		Order buyOrder = orderMap.get(command.buyOrderId());
		Order sellOrder = orderMap.get(command.sellOrderId());

		OrderStatus buyOrderFromStatus = buyOrder.getStatus();
		OrderStatus sellOrderFromStatus = sellOrder.getStatus();

		buyOrder.fill(command.quantity(), command.quoteQty());
		sellOrder.fill(command.quantity(), command.quoteQty());

		Trade buyTrade = Trade.buyOf(command.tradeId(), buyOrder.getSymbol(), command.buyOrderId(), command.sellOrderId(), command.price(), command.quantity(), command.quoteQty(), command.tradeAt());
		Trade sellTrade = Trade.sellOf(command.tradeId(), sellOrder.getSymbol(), command.sellOrderId(), command.buyOrderId(), command.price(), command.quantity(), command.quoteQty(), command.tradeAt());

		OrderHistory buyOrderHistory = OrderHistory.createTransition(buyOrder, buyOrderFromStatus, OrderHisReason.ENGINE_MATCHED);
		OrderHistory sellOrderHistory = OrderHistory.createTransition(sellOrder, sellOrderFromStatus, OrderHisReason.ENGINE_MATCHED);

		tradeRepository.saveAll(List.of(buyTrade, sellTrade));
		orderRepository.updateFill(List.of(buyOrder, sellOrder));
		orderHistoryRepository.saveAll(List.of(buyOrderHistory, sellOrderHistory));
	}
}
