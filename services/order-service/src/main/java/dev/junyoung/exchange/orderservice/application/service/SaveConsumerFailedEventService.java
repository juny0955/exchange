package dev.junyoung.exchange.orderservice.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.junyoung.exchange.orderservice.application.port.in.SaveConsumerFailedEventUseCase;
import dev.junyoung.exchange.orderservice.application.port.in.command.SaveConsumerFailedEventCommand;
import dev.junyoung.exchange.orderservice.application.port.out.ConsumerFailedEventRepository;
import dev.junyoung.exchange.orderservice.domain.model.entity.ConsumerFailedEvent;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SaveConsumerFailedEventService implements SaveConsumerFailedEventUseCase {

    private final ConsumerFailedEventRepository consumeFailedEventRepository;

    @Override
    public void save(SaveConsumerFailedEventCommand command) {
        consumeFailedEventRepository.save(
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