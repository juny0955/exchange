package dev.junyoung.exchange.accountservice.application.service;

import dev.junyoung.exchange.accountservice.application.port.in.SaveEngineFailedEventUseCase;
import dev.junyoung.exchange.accountservice.application.port.in.command.SaveEngineFailedEventCommand;
import dev.junyoung.exchange.accountservice.application.port.out.EngineFailedEventRepository;
import dev.junyoung.exchange.accountservice.domain.model.entity.EngineFailedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SaveEngineFailedEventService implements SaveEngineFailedEventUseCase {

    private final EngineFailedEventRepository engineFailedEventRepository;

    @Override
    public void save(SaveEngineFailedEventCommand command) {
        engineFailedEventRepository.save(
            EngineFailedEvent.create(
                command.topic(),
                command.partition(),
                command.offset(),
                command.payload(),
                command.errorMessage()
            )
        );
    }
}