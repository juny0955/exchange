package dev.junyoung.exchange.orderservice.adapter.out.persistence;

import dev.junyoung.exchange.orderservice.Tables;
import dev.junyoung.exchange.orderservice.domain.model.entity.Order;
import dev.junyoung.exchange.orderservice.tables.records.OrdersRecord;
import org.jooq.DSLContext;

public class JooqOrderMapper {


    static OrdersRecord toRecord(DSLContext dslContext, Order order) {
        OrdersRecord record = dslContext.newRecord(Tables.ORDERS);
        record.setOrderId(order.orderId().value());
        record.setAccountId(order.accountId().value());
        record.setClientOrderId(order.clientOrderId());
        record.setAcceptedSeq(order.acceptedSeq());
        record.setBaseAsset(order.symbol().baseAsset());
        record.setQuoteAsset(order.symbol().quoteAsset());
        record.setSide(order.side().name());
        record.setOrderType(order.orderType().name());
        record.setTif(order.tif().name());
        record.setStatus(order.status().name());
        record.setPrice(order.getPriceValue().orElse(null));
        record.setQuantity(order.getQuantityValue().orElse(null));
        record.setQuoteQty(order.getQuoteValue().orElse(null));
        record.setCumBaseQty(order.cumBaseQty().value());
        record.setCumQuoteQty(order.cumQuoteQty().value());
        record.setOrderedAt(order.orderedAt());
        record.setCreatedAt(order.createdAt());
        record.setUpdatedAt(order.updatedAt());
        return record;
    }
}
