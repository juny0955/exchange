package dev.junyoung.exchange.orderservice.application.port.out;

import dev.junyoung.exchange.orderservice.application.port.out.command.AccountReleaseCommand;
import dev.junyoung.exchange.orderservice.application.port.out.command.AccountReserveCommand;

/**
 * account-service로 직접 요청할일 없어짐 제거예정
 */
@Deprecated
public interface AccountReservationPort {
    void reserve(AccountReserveCommand command);
    void release(AccountReleaseCommand command);
}
