package dev.junyoung.exchange.accountservice.application.port.in;

import dev.junyoung.exchange.accountservice.application.port.in.command.SaveRejectedOutboxCommand;

public interface SaveRejectedOutboxUseCase {
    void save(SaveRejectedOutboxCommand command);
}
