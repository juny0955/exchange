package dev.junyoung.exchange.orderservice.adapter.out.persistence.order;

import dev.junyoung.exchange.orderservice.Tables;
import dev.junyoung.exchange.orderservice.domain.model.entity.Order;
import dev.junyoung.exchange.orderservice.domain.model.enums.OrderStatus;
import dev.junyoung.exchange.orderservice.domain.model.enums.OrderType;
import dev.junyoung.exchange.orderservice.domain.model.enums.Side;
import dev.junyoung.exchange.orderservice.domain.model.enums.TimeInForce;
import dev.junyoung.exchange.orderservice.domain.model.value.*;
import dev.junyoung.exchange.orderservice.tables.records.OrdersRecord;
import org.jooq.DSLContext;

public final class JooqOrderMapper {

    static Order toDomain(OrdersRecord record) {
        return new Order(
            new OrderId(record.getOrderId()),
            new AccountId(record.getAccountId()),
            record.getClientOrderId(),
            record.getAcceptedSeq(),
            new Symbol(record.getBaseAsset(), record.getQuoteAsset()),
            Side.valueOf(record.getSide()),
            OrderType.valueOf(record.getOrderType()),
            TimeInForce.valueOf(record.getTif()),
            record.getPrice() != null ? new Price(record.getPrice()) : null,
            record.getQuantity() != null ? new Quantity(record.getQuantity()) : null,
            record.getQuoteQty() != null ? new QuoteQty(record.getQuoteQty()) : null,
            new Quantity(record.getCumBaseQty()),
            new QuoteQty(record.getCumQuoteQty()),
            OrderStatus.valueOf(record.getStatus()),
            record.getOrderedAt(),
            record.getCreatedAt(),
            record.getUpdatedAt()
        );
    }

    static OrdersRecord toRecord(DSLContext dslContext, Order order) {
        OrdersRecord record = dslContext.newRecord(Tables.ORDERS);
        record.setOrderId(order.getOrderId().value());
        record.setAccountId(order.getAccountId().value());
        record.setClientOrderId(order.getClientOrderId());
        record.setAcceptedSeq(order.getAcceptedSeq());
        record.setBaseAsset(order.getSymbol().baseAsset());
        record.setQuoteAsset(order.getSymbol().quoteAsset());
        record.setSide(order.getSide().name());
        record.setOrderType(order.getOrderType().name());
        record.setTif(order.getTif().name());
        record.setStatus(order.getStatus().name());
        record.setPrice(order.getPrice().orElse(null));
        record.setQuantity(order.getQuantity().orElse(null));
        record.setQuoteQty(order.getQuoteQty().orElse(null));
        record.setCumBaseQty(order.getCumBaseQty().value());
        record.setCumQuoteQty(order.getCumQuoteQty().value());
        record.setOrderedAt(order.getOrderedAt());
        record.setCreatedAt(order.getCreatedAt());
        record.setUpdatedAt(order.getUpdatedAt());
        return record;
    }
}
