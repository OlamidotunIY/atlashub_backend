package com.atlashub.ledger.infrastructure.persistence.adapter;

import com.atlashub.ledger.domain.model.EntryType;
import com.atlashub.ledger.domain.model.LedgerEntry;
import com.atlashub.ledger.domain.model.TransactionReference;
import com.atlashub.ledger.domain.repository.LedgerEntryRepository;
import com.atlashub.ledger.infrastructure.persistence.entity.LedgerEntryJpaEntity;
import com.atlashub.ledger.domain.repository.LedgerEntryRepository;
import com.atlashub.ledger.infrastructure.persistence.entity.LedgerEntryJpaEntity;
import com.atlashub.ledger.infrastructure.persistence.mapper.LedgerEntryMapper;
import com.atlashub.ledger.infrastructure.persistence.repository.SpringDataLedgerEntryRepository;
import com.atlashub.shared.infrastructure.DomainSequenceGenerator;
import org.springframework.stereotype.Repository;

import java.util.List;
import com.atlashub.shared.util.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Repository
public class LedgerEntryRepositoryAdapter implements LedgerEntryRepository {

    private final SpringDataLedgerEntryRepository jpaRepository;
    private final DomainSequenceGenerator sequenceGenerator;
    private final LedgerEntryMapper mapper;

    public LedgerEntryRepositoryAdapter(SpringDataLedgerEntryRepository jpaRepository, DomainSequenceGenerator sequenceGenerator, LedgerEntryMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.sequenceGenerator = sequenceGenerator;
        this.mapper = mapper;
    }

    @Override
    public Long nextIdentity() {
        return sequenceGenerator.nextIdentity("ledger_entry_seq");
    }

    @Override
    public List<LedgerEntry> findByAccountIdAndIdGreaterThan(Long accountId, Long lastEntryId) {
        return jpaRepository.findEntriesAfter(accountId, lastEntryId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<LedgerEntry> findByAccountId(Long accountId) {
        return jpaRepository.findByAccountId(accountId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public PageResult<LedgerEntry> findByAccountIds(List<Long> accountIds, int page, int perPage) {
        Page<LedgerEntryJpaEntity> entityPage = jpaRepository.findByAccountIdInOrderByCreatedAtDesc(
                accountIds, 
                PageRequest.of(page - 1, perPage)
        );
        
        List<LedgerEntry> entries = entityPage.getContent().stream()
                .map(mapper::toDomain)
                .toList();
                
        return new PageResult<>(
                entries,
                page,
                perPage,
                entityPage.getTotalElements(),
                entityPage.getTotalPages()
        );
    }


}
