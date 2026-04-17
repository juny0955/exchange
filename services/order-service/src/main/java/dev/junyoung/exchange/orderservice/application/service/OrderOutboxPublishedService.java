package dev.junyoung.exchange.orderservice.application.service;

import dev.junyoung.exchange.orderservice.application.exception.OrderOutboxNotFoundException;
import dev.junyoung.exchange.orderservice.application.port.in.OrderOutboxPublishedUseCase;
import dev.junyoung.exchange.orderservice.application.port.out.OrderOutboxRepository;
import dev.junyoung.exchange.orderservice.domain.model.entity.OrderOutbox;
import dev.junyoung.exchange.orderservice.domain.model.value.OutboxId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderOutboxPublishedService implements OrderOutboxPublishedUseCase {

    private final OrderOutboxRepository orderOutboxRepository;

    @Override
    public void orderOutboxPublished(OutboxId outboxId) {
        OrderOutbox outbox = orderOutboxRepository.findById(outboxId)
            .orElseThrow(OrderOutboxNotFoundException::new);

        outbox.published();
        orderOutboxRepository.updateStatus(outbox);
    }
}
