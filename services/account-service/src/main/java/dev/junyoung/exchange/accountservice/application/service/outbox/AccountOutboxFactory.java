package dev.junyoung.exchange.accountservice.application.service.outbox;

import org.springframework.stereotype.Component;

import dev.junyoung.exchange.accountservice.application.port.in.command.ReserveBalanceCommand;
import dev.junyoung.exchange.accountservice.application.port.in.command.SaveRejectedOutboxCommand;
import dev.junyoung.exchange.accountservice.application.port.out.event.AccountRejectedEvent;
import dev.junyoung.exchange.accountservice.application.port.out.event.AccountReservedEvent;
import dev.junyoung.exchange.accountservice.domain.model.entity.AccountOutbox;
import dev.junyoung.exchange.accountservice.domain.model.enums.EventType;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class AccountOutboxFactory {

    private final ObjectMapper objectMapper;

    public AccountOutbox reserved(ReserveBalanceCommand command) {
        AccountReservedEvent event = AccountReservedEvent.from(command);
        String payload = objectMapper.writeValueAsString(event);
        return AccountOutbox.create(command.accountId(), EventType.ACCOUNT_RESERVED, command.orderId(), payload);
    }

    public AccountOutbox rejected(SaveRejectedOutboxCommand command) {
        AccountRejectedEvent event = AccountRejectedEvent.from(command);
        String payload = objectMapper.writeValueAsString(event);
        return AccountOutbox.create(command.accountId(), EventType.ACCOUNT_REJECTED, command.orderId(), payload);
    }
}
