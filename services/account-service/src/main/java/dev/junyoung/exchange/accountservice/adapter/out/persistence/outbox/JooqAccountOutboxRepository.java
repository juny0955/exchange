package dev.junyoung.exchange.accountservice.adapter.out.persistence.outbox;

import java.util.Optional;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import dev.junyoung.exchange.accountservice.application.port.out.AccountOutboxRepository;
import dev.junyoung.exchange.accountservice.domain.model.entity.AccountOutbox;
import dev.junyoung.exchange.accountservice.domain.model.value.OutboxId;
import lombok.RequiredArgsConstructor;

import static dev.junyoung.exchange.accountservice.Tables.ACCOUNT_OUTBOX;

@Repository
@RequiredArgsConstructor
public class JooqAccountOutboxRepository implements AccountOutboxRepository {

    private final DSLContext dslContext;

    @Override
    public void save(AccountOutbox outbox) {
        dslContext.insertInto(ACCOUNT_OUTBOX)
            .set(JooqAccountOutboxMapper.toRecord(dslContext, outbox))
            .execute();
    }

    @Override
    public Optional<AccountOutbox> findById(OutboxId outboxId) {
        return Optional.ofNullable(
            dslContext.selectFrom(ACCOUNT_OUTBOX)
                .where(ACCOUNT_OUTBOX.OUTBOX_ID.eq(outboxId.value()))
                .fetchOne(JooqAccountOutboxMapper::toDomain)
        );
    }

    @Override
    public void updateStatus(AccountOutbox outbox) {
        dslContext.update(ACCOUNT_OUTBOX)
            .set(ACCOUNT_OUTBOX.STATUS, outbox.getStatus().name())
            .set(ACCOUNT_OUTBOX.PUBLISHED_AT, outbox.getPublishedAt())
            .where(ACCOUNT_OUTBOX.OUTBOX_ID.eq(outbox.getOutboxId().value()))
            .execute();
    }
}
