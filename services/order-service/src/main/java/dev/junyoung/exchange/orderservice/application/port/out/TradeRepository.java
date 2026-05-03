package dev.junyoung.exchange.orderservice.application.port.out;

import java.util.List;
import java.util.Set;

import dev.junyoung.exchange.orderservice.domain.model.entity.Trade;
import dev.junyoung.exchange.orderservice.domain.model.value.TradeId;

public interface TradeRepository {
	void saveAll(List<Trade> trades);

	Set<TradeId> findExistingTradeIds(List<TradeId> tradeIds);
}
