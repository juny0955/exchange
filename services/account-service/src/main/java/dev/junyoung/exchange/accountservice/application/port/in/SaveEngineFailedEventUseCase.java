package dev.junyoung.exchange.accountservice.application.port.in;

import dev.junyoung.exchange.accountservice.application.port.in.command.SaveEngineFailedEventCommand;

public interface SaveEngineFailedEventUseCase {
    void save(SaveEngineFailedEventCommand command);
}
