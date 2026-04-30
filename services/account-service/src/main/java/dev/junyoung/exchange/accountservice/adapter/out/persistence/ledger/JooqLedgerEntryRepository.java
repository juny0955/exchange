package dev.junyoung.exchange.accountservice.adapter.out.persistence.ledger;

import dev.junyoung.exchange.accountservice.Tables;
import dev.junyoung.exchange.accountservice.application.port.out.LedgerEntryRepository;
import dev.junyoung.exchange.accountservice.domain.model.entity.LedgerEntry;
import dev.junyoung.exchange.accountservice.domain.model.enums.ReferenceType;
import dev.junyoung.exchange.accountservice.tables.records.LedgerEntriesRecord;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JooqLedgerEntryRepository implements LedgerEntryRepository {

    private final DSLContext dslContext;

    @Override
    public void saveAll(List<LedgerEntry> ledgerEntries) {
        List<LedgerEntriesRecord> records = ledgerEntries.stream()
            .map(entry -> JooqLedgerEntryMapper.toRecord(dslContext, entry))
            .toList();

        dslContext.batchInsert(records).execute();
    }

    @Override
    public boolean existsByReferenceTypeAndReferenceId(ReferenceType referenceType, UUID referenceId) {
        return dslContext.fetchExists(
            dslContext.selectOne()
                .from(Tables.LEDGER_ENTRIES)
                .where(Tables.LEDGER_ENTRIES.REFERENCE_TYPE.eq(referenceType.name()))
                .and(Tables.LEDGER_ENTRIES.REFERENCE_ID.eq(referenceId))
        );
    }
}
