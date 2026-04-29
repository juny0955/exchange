package dev.junyoung.exchange.accountservice.adapter.out.persistence.account;

import dev.junyoung.exchange.accountservice.domain.model.entity.Account;
import dev.junyoung.exchange.accountservice.domain.model.enums.AccountStatus;
import dev.junyoung.exchange.accountservice.domain.model.value.AccountId;
import dev.junyoung.exchange.accountservice.tables.records.AccountsRecord;

final class JooqAccountMapper {

    static Account toDomain(AccountsRecord record) {
        return new Account(
            new AccountId(record.getAccountId()),
            AccountStatus.valueOf(record.getStatus()),
            record.getCreatedAt(),
            record.getUpdatedAt()
        );
    }
}
