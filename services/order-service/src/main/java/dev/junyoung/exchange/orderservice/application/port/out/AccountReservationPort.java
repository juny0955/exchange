package dev.junyoung.exchange.orderservice.application.port.out;

import dev.junyoung.exchange.orderservice.application.port.out.command.AccountReleaseCommand;
import dev.junyoung.exchange.orderservice.application.port.out.command.AccountReserveCommand;

public interface AccountReservationPort {
    void reserve(AccountReserveCommand command);
    void release(AccountReleaseCommand command);
}
