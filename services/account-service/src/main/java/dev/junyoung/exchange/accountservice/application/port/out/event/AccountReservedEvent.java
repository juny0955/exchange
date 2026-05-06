package dev.junyoung.exchange.accountservice.application.port.out.event;

import dev.junyoung.exchange.accountservice.application.port.in.command.ReserveBalanceCommand;

import java.util.UUID;

public record AccountReservedEvent(
    UUID orderId,
    UUID accountId,
    String assetCode
) {
    public static AccountReservedEvent from(ReserveBalanceCommand command) {
        return new AccountReservedEvent(
            command.orderId().value(),
            command.accountId().value(),
            command.assetCode().value()
        );
    }
}
