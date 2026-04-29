package dev.junyoung.exchange.accountservice.adapter.out.persistence.ledger;

import dev.junyoung.exchange.accountservice.application.port.out.LedgerEntryRepository;
import dev.junyoung.exchange.accountservice.domain.model.entity.LedgerEntry;
import dev.junyoung.exchange.accountservice.tables.records.LedgerEntriesRecord;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;

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
}
