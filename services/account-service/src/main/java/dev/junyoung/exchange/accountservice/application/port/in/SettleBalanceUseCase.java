package dev.junyoung.exchange.accountservice.application.port.in;

import dev.junyoung.exchange.accountservice.application.port.in.command.SettleBalanceCommand;

import java.util.List;

public interface SettleBalanceUseCase {
    void settle(List<SettleBalanceCommand> commands);
}
