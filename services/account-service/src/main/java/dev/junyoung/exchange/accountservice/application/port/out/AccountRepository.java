package dev.junyoung.exchange.accountservice.application.port.out;

import dev.junyoung.exchange.accountservice.domain.model.entity.Account;
import dev.junyoung.exchange.accountservice.domain.model.value.AccountId;

import java.util.Optional;

public interface AccountRepository {
    Optional<Account> findById(AccountId accountId);
}
