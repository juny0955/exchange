package dev.junyoung.exchange.accountservice.application.port.in;

import dev.junyoung.exchange.accountservice.application.port.in.command.SettleBalanceCommand;

public interface SettleBalanceUseCase {
    void settle(SettleBalanceCommand command);
}
