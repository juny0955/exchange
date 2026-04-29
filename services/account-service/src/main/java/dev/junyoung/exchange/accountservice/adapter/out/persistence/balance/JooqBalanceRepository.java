package dev.junyoung.exchange.accountservice.adapter.out.persistence.balance;

import dev.junyoung.exchange.accountservice.Tables;
import dev.junyoung.exchange.accountservice.application.port.out.BalanceRepository;
import dev.junyoung.exchange.accountservice.domain.model.entity.Balance;
import dev.junyoung.exchange.accountservice.domain.model.value.AccountId;
import dev.junyoung.exchange.accountservice.domain.model.value.AssetCode;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JooqBalanceRepository implements BalanceRepository {

    private final DSLContext dslContext;

    @Override
    public Optional<Balance> findByAccountIdAndAssetCodeForUpdate(AccountId accountId, AssetCode assetCode) {
        return Optional.ofNullable(
            dslContext.selectFrom(Tables.BALANCES)
                .where(Tables.BALANCES.ACCOUNT_ID.eq(accountId.value()))
                .and(Tables.BALANCES.ASSET_CODE.eq(assetCode.value()))
                .forUpdate()
                .fetchOne(JooqBalanceMapper::toDomain)
        );
    }

    @Override
    public void update(Balance balance) {
        dslContext.update(Tables.BALANCES)
            .set(Tables.BALANCES.AVAILABLE, balance.getAvailable())
            .set(Tables.BALANCES.HELD, balance.getHeld())
            .set(Tables.BALANCES.UPDATED_AT, balance.getUpdatedAt())
            .where(Tables.BALANCES.ACCOUNT_ID.eq(balance.getAccountId().value()))
            .and(Tables.BALANCES.ASSET_CODE.eq(balance.getAssetCode().value()))
            .execute();
    }
}
