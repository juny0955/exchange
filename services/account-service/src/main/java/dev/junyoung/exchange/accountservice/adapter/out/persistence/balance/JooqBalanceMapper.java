package dev.junyoung.exchange.accountservice.adapter.out.persistence.balance;

import dev.junyoung.exchange.accountservice.Tables;
import dev.junyoung.exchange.accountservice.domain.model.entity.Balance;
import dev.junyoung.exchange.accountservice.domain.model.value.AccountId;
import dev.junyoung.exchange.accountservice.domain.model.value.AssetCode;
import dev.junyoung.exchange.accountservice.tables.records.BalancesRecord;
import org.jooq.DSLContext;

final class JooqBalanceMapper {

    static Balance toDomain(BalancesRecord record) {
        return new Balance(
            new AccountId(record.getAccountId()),
            new AssetCode(record.getAssetCode()),
            record.getAvailable(),
            record.getHeld(),
            record.getCreatedAt(),
            record.getUpdatedAt()
        );
    }

    static BalancesRecord toRecord(DSLContext dslContext, Balance balance) {
        BalancesRecord record = dslContext.newRecord(Tables.BALANCES);
        record.setAccountId(balance.getAccountId().value());
        record.setAssetCode(balance.getAssetCode().value());
        record.setAvailable(balance.getAvailable());
        record.setHeld(balance.getHeld());
        record.setCreatedAt(balance.getCreatedAt());
        record.setUpdatedAt(balance.getUpdatedAt());
        return record;
    }
}
