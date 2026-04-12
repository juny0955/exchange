package dev.junyoung.exchange.orderservice.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.junyoung.exchange.core.exception.CoreException;
import dev.junyoung.exchange.orderservice.application.exception.OrderErrorCode;
import dev.junyoung.exchange.orderservice.application.port.in.HandleEngineMatchedEventUseCase;
import dev.junyoung.exchange.orderservice.application.port.in.command.EngineMatchedCommand;
import dev.junyoung.exchange.orderservice.application.port.out.OrderHistoryRepository;
import dev.junyoung.exchange.orderservice.application.port.out.OrderRepository;
import dev.junyoung.exchange.orderservice.application.port.out.TradeRepository;
import dev.junyoung.exchange.orderservice.domain.model.entity.Order;
import dev.junyoung.exchange.orderservice.domain.model.entity.OrderHistory;
import dev.junyoung.exchange.orderservice.domain.model.entity.Trade;
import dev.junyoung.exchange.orderservice.domain.model.enums.OrderHisReason;
import dev.junyoung.exchange.orderservice.domain.model.enums.OrderStatus;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class EngineMatchedEventHandler implements HandleEngineMatchedEventUseCase {

	private final OrderRepository orderRepository;
	private final OrderHistoryRepository orderHistoryRepository;
	private final TradeRepository tradeRepository;

	@Override
	public void handle(EngineMatchedCommand command) {
		Order buyOrder = orderRepository.findByIdAndAccountIdForUpdate(command.buyOrderId(), command.buyAccountId())
			.orElseThrow(() -> new CoreException(OrderErrorCode.ORDER_NOT_FOUND));

		Order sellOrder = orderRepository.findByIdAndAccountIdForUpdate(command.sellOrderId(), command.sellAccountId())
			.orElseThrow(() -> new CoreException(OrderErrorCode.ORDER_NOT_FOUND));

		OrderStatus buyOrderFromStatus = buyOrder.getStatus();
		OrderStatus sellOrderFromStatus = sellOrder.getStatus();

		buyOrder.fill(command.quantity(), command.quoteQty());
		sellOrder.fill(command.quantity(), command.quoteQty());

		Trade buyTrade = Trade.buyOf(command.tradeId(), command.symbol(), command.buyOrderId(), command.sellOrderId(), command.price(), command.quantity(), command.quoteQty(), command.tradeAt());
		Trade sellTrade = Trade.sellOf(command.tradeId(), command.symbol(), command.sellOrderId(), command.buyOrderId(), command.price(), command.quantity(), command.quoteQty(), command.tradeAt());

		OrderHistory buyOrderHistory = OrderHistory.createTransition(buyOrder, buyOrderFromStatus, OrderHisReason.ENGINE_MATCHED);
		OrderHistory sellOrderHistory = OrderHistory.createTransition(sellOrder, sellOrderFromStatus, OrderHisReason.ENGINE_MATCHED);

		tradeRepository.saveAll(List.of(buyTrade, sellTrade));
		orderRepository.updateAll(List.of(buyOrder, sellOrder));
		orderHistoryRepository.saveAll(List.of(buyOrderHistory, sellOrderHistory));
	}
}
