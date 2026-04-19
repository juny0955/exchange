package dev.junyoung.exchange.orderservice.adapter.out.persistence.outbox;

import dev.junyoung.exchange.orderservice.domain.model.enums.EventType;
import dev.junyoung.exchange.orderservice.domain.model.enums.OutboxStatus;
import dev.junyoung.exchange.orderservice.domain.model.value.OrderId;
import dev.junyoung.exchange.orderservice.domain.model.value.OutboxId;
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
		record.setAggregateType(orderOutbox.getAggregateType());
		record.setEventType(orderOutbox.getType().name());
		record.setPayload(JSON.json(orderOutbox.getPayload()));
		record.setStatus(orderOutbox.getStatus().name());
		record.setRetryCount(orderOutbox.getRetryCount());
		record.setCreatedAt(orderOutbox.getCreatedAt());
		record.setPublishedAt(orderOutbox.getPublishedAt());
		return record;
	}

	public static OrderOutbox toDomain(OrderOutboxRecord record) {
		return new OrderOutbox(
			new OutboxId(record.getOutboxId()),
			new OrderId(record.getOrderId()),
			record.getAggregateType(),
			EventType.valueOf(record.getEventType()),
			record.getPayload().data(),
			OutboxStatus.valueOf(record.getStatus()),
			record.getRetryCount(),
			record.getCreatedAt(),
			record.getPublishedAt()
		);
	}
}
