package dev.junyoung.exchange.accountservice.application.port.in;

import dev.junyoung.exchange.accountservice.application.port.in.command.ReleaseBalanceCommand;

public interface ReleaseBalanceUseCase {
    void release(ReleaseBalanceCommand command);
}
