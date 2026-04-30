package dev.junyoung.exchange.accountservice.application.port.out;

import dev.junyoung.exchange.accountservice.domain.model.entity.LedgerEntry;
import dev.junyoung.exchange.accountservice.domain.model.enums.ReferenceType;

import java.util.List;
import java.util.UUID;

public interface LedgerEntryRepository {
    void saveAll(List<LedgerEntry> ledgerEntries);

    boolean existsByReferenceTypeAndReferenceId(ReferenceType referenceType, UUID referenceId);
}
