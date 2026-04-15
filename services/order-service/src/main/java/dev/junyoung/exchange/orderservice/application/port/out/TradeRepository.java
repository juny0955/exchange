package dev.junyoung.exchange.orderservice.application.port.out;

import java.util.List;

import dev.junyoung.exchange.orderservice.domain.model.entity.Trade;

public interface TradeRepository {
	void saveAll(List<Trade> trades);
}
