package dev.junyoung.exchange.accountservice.application.port.out.event;

import dev.junyoung.exchange.accountservice.application.port.in.command.SaveRejectedOutboxCommand;
import dev.junyoung.exchange.accountservice.domain.model.enums.RejectedReason;

import java.util.UUID;

public record AccountRejectedEvent(
    UUID orderId,
    UUID accountId,
    String assetCode,
    RejectedReason reason
) {
    public static AccountRejectedEvent from(SaveRejectedOutboxCommand command) {
        return new AccountRejectedEvent(
            command.orderId().value(),
            command.accountId().value(),
            command.assetCode().value(),
            command.reason()
        );
    }
}
