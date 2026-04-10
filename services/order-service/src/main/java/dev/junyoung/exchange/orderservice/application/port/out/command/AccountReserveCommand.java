package dev.junyoung.exchange.orderservice.application.port.out.command;

import dev.junyoung.exchange.orderservice.domain.model.entity.Order;
import dev.junyoung.exchange.orderservice.domain.model.enums.Side;
import dev.junyoung.exchange.orderservice.domain.model.value.*;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountReserveCommand(
    UUID orderId,
    UUID accountId,
    String baseAsset,
    String quoteAsset,
    Side side,
    BigDecimal price,
    BigDecimal quantity,
    BigDecimal quoteQty
) {

    public static AccountReserveCommand from(Order order) {
        return new AccountReserveCommand(
            order.getOrderId().value(),
            order.getAccountId().value(),
            order.getSymbol().baseAsset(),
            order.getSymbol().quoteAsset(),
            order.getSide(),
            order.getPrice().orElse(null),
            order.getQuantity().orElse(null),
            order.getQuoteQty().orElse(null)
        );
    }
}
