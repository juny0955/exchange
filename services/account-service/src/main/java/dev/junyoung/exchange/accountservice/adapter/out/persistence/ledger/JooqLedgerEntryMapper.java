package dev.junyoung.exchange.accountservice.adapter.out.persistence.ledger;

import dev.junyoung.exchange.accountservice.Tables;
import dev.junyoung.exchange.accountservice.domain.model.entity.LedgerEntry;
import dev.junyoung.exchange.accountservice.tables.records.LedgerEntriesRecord;
import org.jooq.DSLContext;

final class JooqLedgerEntryMapper {

    static LedgerEntriesRecord toRecord(DSLContext dslContext, LedgerEntry entry) {
        LedgerEntriesRecord record = dslContext.newRecord(Tables.LEDGER_ENTRIES);
        record.setAccountId(entry.accountId().value());
        record.setAssetCode(entry.assetCode().value());
        record.setAmount(entry.amount());
        record.setBalanceType(entry.balanceType().name());
        record.setEntryType(entry.entryType().name());
        record.setReferenceType(entry.referenceType().name());
        record.setReferenceId(entry.referenceId());
        record.setCreatedAt(entry.createdAt());
        return record;
    }
}
