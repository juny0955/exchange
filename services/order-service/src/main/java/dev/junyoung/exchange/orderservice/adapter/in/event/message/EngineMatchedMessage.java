package dev.junyoung.exchange.orderservice.adapter.in.event.message;

import dev.junyoung.exchange.orderservice.application.port.in.command.EngineMatchedCommand;
import dev.junyoung.exchange.orderservice.domain.model.value.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record EngineMatchedMessage(
    UUID tradeId,
    UUID buyAccountId,
    UUID buyOrderId,
    UUID sellAccountId,
    UUID sellOrderId,
    BigDecimal price,
    BigDecimal quantity,
    BigDecimal quoteQty,
    Instant tradeAt
) {
    public EngineMatchedCommand toCommand() {
        return new EngineMatchedCommand(
          new TradeId(tradeId),
          new AccountId(buyAccountId),
          new OrderId(buyOrderId),
          new AccountId(sellAccountId),
          new OrderId(sellOrderId),
          new Price(price),
          new Quantity(quantity),
          new QuoteQty(quoteQty),
          tradeAt
        );
    }
}
