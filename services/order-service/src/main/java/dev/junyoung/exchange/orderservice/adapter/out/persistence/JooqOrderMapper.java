package dev.junyoung.exchange.orderservice.adapter.out.persistence;

import dev.junyoung.exchange.orderservice.Tables;
import dev.junyoung.exchange.orderservice.domain.model.entity.Order;
import dev.junyoung.exchange.orderservice.tables.records.OrdersRecord;
import org.jooq.DSLContext;

public class JooqOrderMapper {


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
