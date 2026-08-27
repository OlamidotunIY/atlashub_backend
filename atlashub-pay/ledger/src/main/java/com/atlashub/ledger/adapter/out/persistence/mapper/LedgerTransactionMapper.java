package com.atlashub.ledger.adapter.out.persistence.mapper;

import com.atlashub.ledger.domain.model.LedgerTransaction;
import com.atlashub.ledger.adapter.out.persistence.entity.LedgerEntryJpaEntity;
import com.atlashub.ledger.adapter.out.persistence.entity.LedgerTransactionJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LedgerTransactionMapper {

    private final LedgerEntryMapper entryMapper;

    public LedgerTransactionMapper(LedgerEntryMapper entryMapper) {
        this.entryMapper = entryMapper;
    }

    public LedgerTransactionJpaEntity toEntity(LedgerTransaction domain) {
        if (domain == null) return null;

        List<LedgerEntryJpaEntity> entryEntities = domain.getEntries().stream()
                .map(entryMapper::toEntity)
                .toList();

        LedgerTransactionJpaEntity transactionEntity = new LedgerTransactionJpaEntity(
                domain.getId(),
                domain.getTransactionReference().transactionId(),
                domain.getTransactionReference().sourceSystem().name(),
                domain.getPostedAt(),
                entryEntities
        );

        for (LedgerEntryJpaEntity entryEntity : entryEntities) {
            entryEntity.setTransaction(transactionEntity);
        }

        return transactionEntity;
    }

    // toDomain not strictly required yet for ledger_transactions as it is append-only, 
    // but typically we'd add it here if we need to load transactions by ID.
}
