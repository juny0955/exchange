package dev.junyoung.exchange.accountservice.application.port.in;

import dev.junyoung.exchange.accountservice.application.port.in.command.SaveConsumerFailedEventCommand;

public interface SaveConsumerFailedEventUseCase {
    void save(SaveConsumerFailedEventCommand command);
}
