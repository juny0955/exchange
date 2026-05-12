package dev.junyoung.exchange.accountservice.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import dev.junyoung.exchange.accountservice.application.port.in.SaveRejectedOutboxUseCase;
import dev.junyoung.exchange.accountservice.application.port.in.command.SaveRejectedOutboxCommand;
import dev.junyoung.exchange.accountservice.application.port.out.AccountOutboxRepository;
import dev.junyoung.exchange.accountservice.application.service.outbox.AccountOutboxFactory;
import dev.junyoung.exchange.accountservice.domain.model.entity.AccountOutbox;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
@Slf4j
public class SaveRejectedOutboxService implements SaveRejectedOutboxUseCase {

    private final AccountOutboxRepository accountOutboxRepository;
    private final AccountOutboxFactory accountOutboxFactory;
    private final MeterRegistry meterRegistry;

    @Override
    @NewSpan("account.reserve.rejected")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(SaveRejectedOutboxCommand command) {
        AccountOutbox outbox = accountOutboxFactory.rejected(command);
        accountOutboxRepository.save(outbox);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                Counter.builder("account.reserve.failure")
                    .register(meterRegistry)
                    .increment();
            }
        });

        log.info("[RESERVE_REJECTED] 잔고 예약 거부 outbox 저장. orderId={}, reason={}",
            command.orderId().value(), command.reason());
    }
}
