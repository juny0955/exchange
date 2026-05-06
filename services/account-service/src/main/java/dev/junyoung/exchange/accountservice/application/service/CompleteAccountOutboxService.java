package dev.junyoung.exchange.accountservice.application.service;

import dev.junyoung.exchange.accountservice.application.exception.AccountOutboxNotFoundException;
import dev.junyoung.exchange.accountservice.application.port.in.CompleteAccountOutboxUseCase;
import dev.junyoung.exchange.accountservice.application.port.out.AccountOutboxRepository;
import dev.junyoung.exchange.accountservice.domain.model.entity.AccountOutbox;
import dev.junyoung.exchange.accountservice.domain.model.value.OutboxId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class CompleteAccountOutboxService implements CompleteAccountOutboxUseCase {

    private final AccountOutboxRepository accountOutboxRepository;

    @Override
    public void complete(OutboxId outboxId, Instant publishedAt) {
        AccountOutbox outbox = accountOutboxRepository.findById(outboxId)
            .orElseThrow(AccountOutboxNotFoundException::new);

        boolean completed = outbox.complete(publishedAt);
        if (!completed) return;

        accountOutboxRepository.updateStatus(outbox);
    }
}
