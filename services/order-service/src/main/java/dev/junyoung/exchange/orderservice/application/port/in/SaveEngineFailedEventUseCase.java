package dev.junyoung.exchange.orderservice.application.port.in;

import dev.junyoung.exchange.orderservice.application.port.in.command.SaveEngineFailedEventCommand;

public interface SaveEngineFailedEventUseCase {
    void save(SaveEngineFailedEventCommand command);
}
