package dev.junyoung.exchange.orderservice.application.service.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.junyoung.exchange.orderservice.application.exception.OrderNotFoundException;
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
import dev.junyoung.exchange.orderservice.domain.model.value.TradeId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class EngineMatchedEventHandler implements HandleEngineMatchedEventUseCase {

	private final OrderRepository orderRepository;
	private final OrderHistoryRepository orderHistoryRepository;
	private final TradeRepository tradeRepository;

	@Override
	public void handle(List<EngineMatchedCommand> commands) {
		if (commands.isEmpty()) return;

		List<EngineMatchedCommand> newCommands = filterDuplicate(commands);
		if (newCommands.isEmpty()) return;

		Map<OrderId, Order> orderMap = loadOrders(newCommands);

		MatchResult result = processMatches(newCommands, orderMap);

		tradeRepository.saveAll(result.trades());
		orderRepository.updateFill(List.copyOf(orderMap.values()));
		orderHistoryRepository.saveAll(result.histories());
	}

	private List<EngineMatchedCommand> filterDuplicate(List<EngineMatchedCommand> commands) {
		List<TradeId> tradeIds = commands.stream()
			.map(EngineMatchedCommand::tradeId)
			.toList();
		Set<TradeId> existingTradeIds = tradeRepository.findExistingTradeIds(tradeIds);

		List<EngineMatchedCommand> newCommands = commands.stream()
			.filter(c -> !existingTradeIds.contains(c.tradeId()))
			.toList();

		int skipped = commands.size() - newCommands.size();
		if (skipped > 0) {
			log.debug("[ENGINE_MATCHED: duplicate] 이미 처리된 매칭 무시. total={}, skipped={}",
				commands.size(), skipped);
		}

		return newCommands;
	}

	private Map<OrderId, Order> loadOrders(List<EngineMatchedCommand> commands) {
		List<OrderId> orderIds = commands.stream()
			.flatMap(c -> Stream.of(c.buyOrderId(), c.sellOrderId()))
			.distinct()
			.toList();

		List<Order> orders = orderRepository.findAllByIdForUpdate(orderIds);

		// NPE 방어용 (터질 가능성 거의 없음)
		if (orders.size() != orderIds.size())
			throw new OrderNotFoundException();

		return orders.stream()
			.collect(Collectors.toMap(Order::getOrderId, Function.identity()));
	}

	private MatchResult processMatches(List<EngineMatchedCommand> commands, Map<OrderId, Order> orderMap) {
		List<Trade> trades = new ArrayList<>();
		List<OrderHistory> histories = new ArrayList<>();

		for (EngineMatchedCommand command : commands) {
			Order buyOrder = orderMap.get(command.buyOrderId());
			Order sellOrder = orderMap.get(command.sellOrderId());

			OrderStatus buyFromStatus = buyOrder.getStatus();
			OrderStatus sellFromStatus = sellOrder.getStatus();

			buyOrder.fill(command.quantity(), command.quoteQty());
			sellOrder.fill(command.quantity(), command.quoteQty());

			trades.addAll(createTrades(command, buyOrder, sellOrder));
			histories.add(OrderHistory.createTransition(buyOrder, buyFromStatus, OrderHisReason.ENGINE_MATCHED));
			histories.add(OrderHistory.createTransition(sellOrder, sellFromStatus, OrderHisReason.ENGINE_MATCHED));
		}

		return new MatchResult(trades, histories);
	}

	private List<Trade> createTrades(EngineMatchedCommand command, Order buyOrder, Order sellOrder) {
		return List.of(
			Trade.buyOf(
				command.tradeId(), buyOrder.getSymbol(),
				command.buyOrderId(), command.sellOrderId(),
				command.price(), command.quantity(), command.quoteQty(), command.tradeAt()
			),
			Trade.sellOf(
				command.tradeId(), sellOrder.getSymbol(),
				command.sellOrderId(), command.buyOrderId(),
				command.price(), command.quantity(), command.quoteQty(), command.tradeAt()
			)
		);
	}

	private record MatchResult(List<Trade> trades, List<OrderHistory> histories) {}
}
