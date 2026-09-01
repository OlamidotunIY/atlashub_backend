package com.atlashub.ledger.adapter.out.persistence;

import com.atlashub.ledger.domain.model.LedgerTransaction;
import com.atlashub.ledger.domain.repository.LedgerTransactionRepository;
import com.atlashub.ledger.adapter.out.persistence.entity.LedgerTransactionJpaEntity;
import com.atlashub.ledger.adapter.out.persistence.mapper.LedgerTransactionMapper;
import com.atlashub.ledger.adapter.out.persistence.repository.SpringDataLedgerTransactionRepository;
import com.atlashub.ledger.domain.valueobject.SourceSystem;
import com.atlashub.shared.adapter.out.external.DomainSequenceGenerator;
import org.springframework.stereotype.Repository;

@Repository
public class LedgerTransactionRepositoryAdapter implements LedgerTransactionRepository {

    private final SpringDataLedgerTransactionRepository jpaRepository;
    private final DomainSequenceGenerator sequenceGenerator;
    private final LedgerTransactionMapper mapper;

    public LedgerTransactionRepositoryAdapter(SpringDataLedgerTransactionRepository jpaRepository, DomainSequenceGenerator sequenceGenerator, LedgerTransactionMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.sequenceGenerator = sequenceGenerator;
        this.mapper = mapper;
    }

    @Override
    public Long nextIdentity() {
        return sequenceGenerator.nextIdentity("ledger_transaction_seq");
    }

    @Override
    public LedgerTransaction save(LedgerTransaction transaction) {
        LedgerTransactionJpaEntity entity = mapper.toEntity(transaction);
        LedgerTransactionJpaEntity savedEntity = jpaRepository.save(entity);

        // Ensure we dispatch domain events via outbox later. For now, aggregate tracks them.
        return transaction;
    }

    @Override
    public boolean existsByReference(String transactionId, SourceSystem sourceSystem) {
        return jpaRepository.existsBySourceTransaction(transactionId, sourceSystem.name());
    }
}
