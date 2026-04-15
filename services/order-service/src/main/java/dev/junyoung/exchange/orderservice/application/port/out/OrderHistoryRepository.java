package dev.junyoung.exchange.orderservice.application.port.out;

import java.util.List;

import dev.junyoung.exchange.orderservice.domain.model.entity.OrderHistory;

public interface OrderHistoryRepository {
	void save(OrderHistory orderHistory);
	void saveAll(List<OrderHistory> orderHistories);
}
