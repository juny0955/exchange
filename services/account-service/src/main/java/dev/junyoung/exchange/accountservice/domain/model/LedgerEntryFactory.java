package dev.junyoung.exchange.accountservice.domain.model;

import dev.junyoung.exchange.accountservice.domain.model.entity.LedgerEntry;
import dev.junyoung.exchange.accountservice.domain.model.enums.BalanceType;
import dev.junyoung.exchange.accountservice.domain.model.enums.EntryType;
import dev.junyoung.exchange.accountservice.domain.model.enums.ReferenceType;
import dev.junyoung.exchange.accountservice.domain.model.value.AccountId;
import dev.junyoung.exchange.accountservice.domain.model.value.AssetCode;
import dev.junyoung.exchange.accountservice.domain.model.value.OrderId;

import java.math.BigDecimal;
import java.util.List;

public final class LedgerEntryFactory {

    public static List<LedgerEntry> createForOrder(
        AccountId accountId,
        AssetCode assetCode,
        BigDecimal amount,
        OrderId orderId
    ) {
        return List.of(
            LedgerEntry.create(accountId, assetCode, amount, BalanceType.AVAILABLE, EntryType.DEBIT,    ReferenceType.ORDER, orderId.value()),
            LedgerEntry.create(accountId, assetCode, amount, BalanceType.HELD,      EntryType.CREDIT,   ReferenceType.ORDER, orderId.value())
        );
    }

    public static List<LedgerEntry> createForRelease(AccountId accountId, AssetCode assetCode, BigDecimal amount, OrderId orderId) {
        return List.of(
            LedgerEntry.create(accountId, assetCode, amount, BalanceType.HELD,      EntryType.DEBIT,    ReferenceType.ORDER, orderId.value()),
            LedgerEntry.create(accountId, assetCode, amount, BalanceType.AVAILABLE, EntryType.CREDIT,   ReferenceType.ORDER, orderId.value())
        );
    }
}
