package dev.junyoung.exchange.accountservice.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import dev.junyoung.exchange.accountservice.application.port.in.SaveRejectedOutboxUseCase;
import dev.junyoung.exchange.accountservice.application.port.in.command.SaveRejectedOutboxCommand;
import dev.junyoung.exchange.accountservice.application.port.out.AccountOutboxRepository;
import dev.junyoung.exchange.accountservice.application.service.outbox.AccountOutboxFactory;
import dev.junyoung.exchange.accountservice.domain.model.entity.AccountOutbox;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SaveRejectedOutboxService implements SaveRejectedOutboxUseCase {

    private final AccountOutboxRepository accountOutboxRepository;
    private final AccountOutboxFactory accountOutboxFactory;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(SaveRejectedOutboxCommand command) {
        AccountOutbox outbox = accountOutboxFactory.rejected(command);
        accountOutboxRepository.save(outbox);
    }
}
