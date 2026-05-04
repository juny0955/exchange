package dev.junyoung.exchange.accountservice.application.service;

import dev.junyoung.exchange.accountservice.application.port.in.SaveConsumerFailedEventUseCase;
import dev.junyoung.exchange.accountservice.application.port.in.command.SaveConsumerFailedEventCommand;
import dev.junyoung.exchange.accountservice.application.port.out.ConsumerFailedEventRepository;
import dev.junyoung.exchange.accountservice.domain.model.entity.ConsumerFailedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SaveConsumerFailedEventService implements SaveConsumerFailedEventUseCase {

    private final ConsumerFailedEventRepository consumerFailedEventRepository;

    @Override
    public void save(SaveConsumerFailedEventCommand command) {
        consumerFailedEventRepository.save(
            ConsumerFailedEvent.create(
                command.topic(),
                command.partition(),
                command.offset(),
                command.payload(),
                command.errorMessage()
            )
        );
    }
}