package dev.junyoung.exchange.orderservice.application.port.in;

import dev.junyoung.exchange.orderservice.application.port.in.command.SaveConsumerFailedEventCommand;

public interface SaveConsumerFailedEventUseCase {
    void save(SaveConsumerFailedEventCommand command);
}
