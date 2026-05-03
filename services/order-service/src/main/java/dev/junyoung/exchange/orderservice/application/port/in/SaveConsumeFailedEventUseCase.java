package dev.junyoung.exchange.orderservice.application.port.in;

import dev.junyoung.exchange.orderservice.application.port.in.command.SaveConsumeFailedEventCommand;

public interface SaveConsumeFailedEventUseCase {
    void save(SaveConsumeFailedEventCommand command);
}
