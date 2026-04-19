package dev.junyoung.exchange.orderservice.adapter.out.persistence.outbox;

import dev.junyoung.exchange.orderservice.application.port.out.OrderOutboxRepository;
import dev.junyoung.exchange.orderservice.domain.model.entity.OrderOutbox;
import dev.junyoung.exchange.orderservice.domain.model.value.OutboxId;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static dev.junyoung.exchange.orderservice.Tables.ORDER_OUTBOX;

@Repository
@RequiredArgsConstructor
public class JooqOrderOutboxRepository implements OrderOutboxRepository {

	private final DSLContext dslContext;

	@Override
	public void save(OrderOutbox orderOutbox) {
		dslContext.insertInto(ORDER_OUTBOX)
			.set(JooqOrderOutboxMapper.toRecord(dslContext, orderOutbox))
			.execute();
	}

	@Override
	public void updateStatus(OrderOutbox outbox) {
		dslContext.update(ORDER_OUTBOX)
			.set(ORDER_OUTBOX.STATUS, outbox.getStatus().name())
			.set(ORDER_OUTBOX.PUBLISHED_AT, outbox.getPublishedAt())
			.where(ORDER_OUTBOX.OUTBOX_ID.eq(outbox.getOutboxId().value()))
			.execute();
	}

	@Override
	public Optional<OrderOutbox> findById(OutboxId outboxId) {
		return Optional.ofNullable(
			dslContext.selectFrom(ORDER_OUTBOX)
				.where(ORDER_OUTBOX.OUTBOX_ID.eq(outboxId.value()))
				.fetchOne(JooqOrderOutboxMapper::toDomain)
		);
	}
}
