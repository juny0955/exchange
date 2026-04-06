package dev.junyoung.exchange.orderservice.application.port.out;

import dev.junyoung.exchange.orderservice.domain.model.entity.Order;
import dev.junyoung.exchange.orderservice.domain.model.value.AccountId;
import dev.junyoung.exchange.orderservice.domain.model.value.OrderId;

import java.util.Optional;

public interface OrderRepository {
    void save(Order order);
    boolean existsByAccountIdAndClientOrderId(AccountId accountId, String clientOrderId);

    Optional<Order> findByIdAndAccountId(OrderId orderId, AccountId accountId);
}
