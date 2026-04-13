package dev.junyoung.exchange.orderservice.adapter.out.persistence;

import org.jooq.DSLContext;
import org.jooq.JSON;

import dev.junyoung.exchange.orderservice.Tables;
import dev.junyoung.exchange.orderservice.domain.model.entity.OrderOutbox;
import dev.junyoung.exchange.orderservice.tables.records.OrderOutboxRecord;

public final class JooqOrderOutboxMapper {

	public static OrderOutboxRecord toRecord(DSLContext dslContext, OrderOutbox orderOutbox) {
		OrderOutboxRecord record = dslContext.newRecord(Tables.ORDER_OUTBOX);
		record.setOutboxId(orderOutbox.getOutboxId().value());
		record.setOrderId(orderOutbox.getOrderId().value());
		record.setPayload(JSON.json(orderOutbox.getPayload()));
		record.setStatus(orderOutbox.getStatus().name());
		record.setRetryCount(orderOutbox.getRetryCount());
		record.setCreatedAt(orderOutbox.getCreatedAt());
		record.setPublishedAt(orderOutbox.getPublishedAt());
		return record;
	}
}
