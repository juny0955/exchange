package dev.junyoung.exchange.orderservice.application.port.out.command;

import java.math.BigDecimal;
import java.util.UUID;

import dev.junyoung.exchange.orderservice.domain.model.entity.Order;
import dev.junyoung.exchange.orderservice.domain.service.AssetReserveCalculator;
import dev.junyoung.exchange.orderservice.domain.service.dto.AssetReserveResult;

public record AccountReserveCommand(
    UUID orderId,
    UUID accountId,
    String asset,
    BigDecimal amount
) {

    public static AccountReserveCommand from(Order order) {
        AssetReserveResult calculate = AssetReserveCalculator.calculate(order);

        return new AccountReserveCommand(
            order.getOrderId().value(),
            order.getAccountId().value(),
            calculate.asset(),
            calculate.amount()
        );
    }
}
