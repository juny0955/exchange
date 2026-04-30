package dev.junyoung.exchange.accountservice.adapter.out.persistence.balance;

import dev.junyoung.exchange.accountservice.Tables;
import dev.junyoung.exchange.accountservice.application.port.out.BalanceLockKey;
import dev.junyoung.exchange.accountservice.application.port.out.BalanceRepository;
import dev.junyoung.exchange.accountservice.domain.model.entity.Balance;
import dev.junyoung.exchange.accountservice.domain.model.value.AccountId;
import dev.junyoung.exchange.accountservice.domain.model.value.AssetCode;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    @Override
    public void upsertAll(List<Balance> balances) {
        if (balances.isEmpty()) return;

        var queries = balances.stream()
            .map(balance -> dslContext.insertInto(Tables.BALANCES)
                .set(JooqBalanceMapper.toRecord(dslContext, balance))
                .onConflict(Tables.BALANCES.ACCOUNT_ID, Tables.BALANCES.ASSET_CODE)
                .doUpdate()
                .set(Tables.BALANCES.AVAILABLE, balance.getAvailable())
                .set(Tables.BALANCES.HELD, balance.getHeld())
                .set(Tables.BALANCES.UPDATED_AT, balance.getUpdatedAt()))
            .toList();

        dslContext.batch(queries).execute();
    }

    @Override
    public List<Balance> findAllByLockKeyForUpdate(List<BalanceLockKey> keys) {
        if (keys.isEmpty()) return List.of();

        var rows = keys.stream()
            .map(k -> DSL.row(k.accountId().value(), k.assetCode().value()))
            .toList();

        return dslContext.selectFrom(Tables.BALANCES)
            .where(DSL.row(Tables.BALANCES.ACCOUNT_ID, Tables.BALANCES.ASSET_CODE).in(rows))
            .orderBy(
                Tables.BALANCES.ACCOUNT_ID.asc(),
                Tables.BALANCES.ASSET_CODE.asc()
            )
            .forUpdate()
            .fetch(JooqBalanceMapper::toDomain);
    }
}
