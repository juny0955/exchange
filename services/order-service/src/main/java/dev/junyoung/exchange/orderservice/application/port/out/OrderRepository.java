package dev.junyoung.exchange.orderservice.application.port.out;

import dev.junyoung.exchange.orderservice.domain.model.entity.Order;
import dev.junyoung.exchange.orderservice.domain.model.value.AccountId;
import dev.junyoung.exchange.orderservice.domain.model.value.OrderId;

import java.util.Optional;

// TODO DB 예외 정의 필요
public interface OrderRepository {
    void save(Order order);
    void updateStatus(Order order);

    boolean existsByAccountIdAndClientOrderId(AccountId accountId, String clientOrderId);
    Optional<Order> findByIdAndAccountIdForUpdate(OrderId orderId, AccountId accountId);
}
