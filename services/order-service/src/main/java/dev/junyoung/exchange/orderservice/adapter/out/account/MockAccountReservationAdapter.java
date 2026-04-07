package dev.junyoung.exchange.orderservice.adapter.out.account;

import dev.junyoung.exchange.orderservice.application.port.out.AccountReservationPort;
import dev.junyoung.exchange.orderservice.application.port.out.command.AccountReserveCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

// TODO gRPC 구현체 변경 필요
@Component
@Slf4j
public class MockAccountReservationAdapter implements AccountReservationPort {

    @Override
    public void reserve(AccountReserveCommand command) {
        log.info("reserve order: {}", command);
    }
}
