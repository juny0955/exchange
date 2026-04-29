package dev.junyoung.exchange.accountservice.application.port.out;

import dev.junyoung.exchange.accountservice.domain.model.entity.LedgerEntry;

import java.util.List;

public interface LedgerEntryRepository {
    void saveAll(List<LedgerEntry> ledgerEntries);
}
