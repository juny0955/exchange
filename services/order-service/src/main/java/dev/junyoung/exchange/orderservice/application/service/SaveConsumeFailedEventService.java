package dev.junyoung.exchange.orderservice.application.service;

import dev.junyoung.exchange.orderservice.application.port.in.SaveConsumeFailedEventUseCase;
import dev.junyoung.exchange.orderservice.application.port.in.command.SaveConsumeFailedEventCommand;
import dev.junyoung.exchange.orderservice.application.port.out.ConsumeFailedEventRepository;
import dev.junyoung.exchange.orderservice.domain.model.entity.ConsumeFailedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SaveConsumeFailedEventService implements SaveConsumeFailedEventUseCase {

    private final ConsumeFailedEventRepository consumeFailedEventRepository;

    @Override
    public void save(SaveConsumeFailedEventCommand command) {
        consumeFailedEventRepository.save(
            ConsumeFailedEvent.create(
                command.topic(),
                command.partition(),
                command.offset(),
                command.payload(),
                command.errorMessage()
            )
        );
    }
}