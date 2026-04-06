package dev.junyoung.exchange.orderservice.application.port.out;

import dev.junyoung.exchange.orderservice.domain.model.entity.Order;
import dev.junyoung.exchange.orderservice.domain.model.value.AccountId;

public interface OrderRepository {
    void save(Order order);
    boolean existsByAccountIdAndClientOrderId(AccountId accountId, String clientOrderId);
}
