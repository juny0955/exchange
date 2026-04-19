package dev.junyoung.exchange.orderservice.application.service;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.junyoung.exchange.orderservice.application.exception.OrderOutboxNotFoundException;
import dev.junyoung.exchange.orderservice.application.port.in.CompleteOrderOutboxUseCase;
import dev.junyoung.exchange.orderservice.application.port.out.OrderOutboxRepository;
import dev.junyoung.exchange.orderservice.domain.model.entity.OrderOutbox;
import dev.junyoung.exchange.orderservice.domain.model.value.OutboxId;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CompleteOrderOutboxService implements CompleteOrderOutboxUseCase {

    private final OrderOutboxRepository orderOutboxRepository;

    @Override
    public void completeOrderOutbox(OutboxId outboxId, Instant publishedAt) {
        OrderOutbox outbox = orderOutboxRepository.findById(outboxId)
            .orElseThrow(OrderOutboxNotFoundException::new);

        boolean changed = outbox.complete(publishedAt);
        if (!changed) return;

        orderOutboxRepository.updateStatus(outbox);
    }
}
