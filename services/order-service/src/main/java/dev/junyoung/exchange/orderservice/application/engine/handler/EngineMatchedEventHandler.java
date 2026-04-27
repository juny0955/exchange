package dev.junyoung.exchange.orderservice.application.engine.handler;

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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional
public class EngineMatchedEventHandler implements HandleEngineMatchedEventUseCase {

	private final OrderRepository orderRepository;
	private final OrderHistoryRepository orderHistoryRepository;
	private final TradeRepository tradeRepository;

	@Override
	public void handle(List<EngineMatchedCommand> commands) {
		List<OrderId> orderIds = commands.stream()
			.flatMap(c -> Stream.of(c.buyOrderId(), c.sellOrderId()))
			.distinct()
			.toList();

		List<Order> orders = orderRepository.findAllByIdForUpdate(orderIds);

		// NPE 방어용 (터질 가능성 거의 없음)
		if (orders.size() != orderIds.size())
			throw new OrderNotFoundException();

		Map<OrderId, Order> orderMap = orders.stream()
			.collect(Collectors.toMap(Order::getOrderId, Function.identity()));

		List<Trade> trades = new ArrayList<>();
		List<OrderHistory> histories = new ArrayList<>();
		for (EngineMatchedCommand command : commands) {
			Order buyOrder = orderMap.get(command.buyOrderId());
			Order sellOrder = orderMap.get(command.sellOrderId());

			OrderStatus buyOrderFromStatus = buyOrder.getStatus();
			OrderStatus sellOrderFromStatus = sellOrder.getStatus();

			buyOrder.fill(command.quantity(), command.quoteQty());
			sellOrder.fill(command.quantity(), command.quoteQty());

			// TODO Trade 하나로 갈지 두개로 갈지 고민
			trades.add(Trade.buyOf(command.tradeId(), buyOrder.getSymbol(), command.buyOrderId(), command.sellOrderId(), command.price(), command.quantity(), command.quoteQty(), command.tradeAt()));
			trades.add(Trade.sellOf(command.tradeId(), sellOrder.getSymbol(), command.sellOrderId(), command.buyOrderId(), command.price(), command.quantity(), command.quoteQty(), command.tradeAt()));

			histories.add(OrderHistory.createTransition(buyOrder, buyOrderFromStatus, OrderHisReason.ENGINE_MATCHED));
			histories.add(OrderHistory.createTransition(sellOrder, sellOrderFromStatus, OrderHisReason.ENGINE_MATCHED));
		}

		tradeRepository.saveAll(trades);
		orderRepository.updateFill(new ArrayList<>(orderMap.values()));
		orderHistoryRepository.saveAll(histories);
	}
}
