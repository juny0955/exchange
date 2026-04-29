package dev.junyoung.exchange.accountservice.adapter.out.persistence.account;

import dev.junyoung.exchange.accountservice.Tables;
import dev.junyoung.exchange.accountservice.application.port.out.AccountRepository;
import dev.junyoung.exchange.accountservice.domain.model.entity.Account;
import dev.junyoung.exchange.accountservice.domain.model.value.AccountId;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JooqAccountRepository implements AccountRepository {

    private final DSLContext dslContext;

    @Override
    public Optional<Account> findById(AccountId accountId) {
        return Optional.ofNullable(
            dslContext.selectFrom(Tables.ACCOUNTS)
                .where(Tables.ACCOUNTS.ACCOUNT_ID.eq(accountId.value()))
                .fetchOne(JooqAccountMapper::toDomain)
        );
    }
}
