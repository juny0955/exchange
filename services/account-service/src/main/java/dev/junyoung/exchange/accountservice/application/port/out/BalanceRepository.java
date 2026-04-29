package dev.junyoung.exchange.accountservice.application.port.out;

import dev.junyoung.exchange.accountservice.domain.model.entity.Balance;
import dev.junyoung.exchange.accountservice.domain.model.value.AccountId;
import dev.junyoung.exchange.accountservice.domain.model.value.AssetCode;

import java.util.Optional;

public interface BalanceRepository {
    void update(Balance balance);

    Optional<Balance> findByAccountIdAndAssetCodeForUpdate(AccountId accountId, AssetCode assetCode);
}
