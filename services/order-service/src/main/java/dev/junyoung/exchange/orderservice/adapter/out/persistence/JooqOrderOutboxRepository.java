package dev.junyoung.exchange.orderservice.adapter.out.persistence;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import dev.junyoung.exchange.orderservice.Tables;
import dev.junyoung.exchange.orderservice.application.port.out.OrderOutboxRepository;
import dev.junyoung.exchange.orderservice.domain.model.entity.OrderOutbox;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class JooqOrderOutboxRepository implements OrderOutboxRepository {

	private final DSLContext dslContext;

	@Override
	public void save(OrderOutbox orderOutbox) {
		dslContext.insertInto(Tables.ORDER_OUTBOX)
			.set(JooqOrderOutboxMapper.toRecord(dslContext, orderOutbox))
			.execute();
	}
}
