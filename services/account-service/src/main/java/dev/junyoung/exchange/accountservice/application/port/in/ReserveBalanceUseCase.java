package dev.junyoung.exchange.accountservice.application.port.in;

import dev.junyoung.exchange.accountservice.application.port.in.command.ReserveBalanceCommand;

public interface ReserveBalanceUseCase {
    void reserve(ReserveBalanceCommand command);
}
