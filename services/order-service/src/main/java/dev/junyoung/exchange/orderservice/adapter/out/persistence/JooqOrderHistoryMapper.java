package dev.junyoung.exchange.orderservice.adapter.out.persistence;

import org.jooq.DSLContext;

import dev.junyoung.exchange.orderservice.Tables;
import dev.junyoung.exchange.orderservice.domain.model.entity.OrderHistory;
import dev.junyoung.exchange.orderservice.tables.records.OrderHistoryRecord;

public final class JooqOrderHistoryMapper {

	static OrderHistoryRecord toRecord(DSLContext dslContext, OrderHistory orderHistory) {
		OrderHistoryRecord record = dslContext.newRecord(Tables.ORDER_HISTORY);
		record.setOrderHistoryId(orderHistory.orderHistoryId());
		record.setOrderId(orderHistory.orderId().value());
		record.setFromStatus(orderHistory.fromStatus() != null ? orderHistory.fromStatus().name() : null);
		record.setToStatus(orderHistory.toStatus().name());
		record.setReason(orderHistory.reason().name());
		record.setDetail(orderHistory.detail());
		record.setCreatedAt(orderHistory.createdAt());
		return record;
	}
}
