package dev.junyoung.exchange.orderservice.application.port.out.command;

import dev.junyoung.exchange.orderservice.domain.model.entity.Order;
import dev.junyoung.exchange.orderservice.domain.model.enums.OrderType;
import dev.junyoung.exchange.orderservice.domain.model.enums.Side;
import dev.junyoung.exchange.orderservice.domain.model.enums.TimeInForce;

import java.math.BigDecimal;
import java.util.UUID;

public record PlaceOrderEvent(
    UUID orderId,
    UUID accountId,
    OrderSymbolEvent symbol,
    Side side,
    OrderType orderType,
    TimeInForce tif,
    BigDecimal price,
    BigDecimal quantity,
    BigDecimal quoteQty
) {
    public static PlaceOrderEvent of(Order order) {
        return new PlaceOrderEvent(
            order.getOrderId().value(),
            order.getAccountId().value(),
            OrderSymbolEvent.of(order.getSymbol()),
            order.getSide(),
            order.getOrderType(),
            order.getTif(),
            order.getPrice().orElse(null),
            order.getQuantity().orElse(null),
            order.getQuoteQty().orElse(null)
        );
    }
}
