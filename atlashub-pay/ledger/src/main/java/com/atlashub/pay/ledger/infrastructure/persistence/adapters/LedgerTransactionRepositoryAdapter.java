package com.atlashub.pay.ledger.infrastructure.persistence.adapters;

import com.atlashub.pay.ledger.domain.entities.LedgerTransaction;
import com.atlashub.pay.ledger.domain.repositories.LedgerTransactionRepository;
import com.atlashub.pay.ledger.infrastructure.persistence.entities.LedgerEntryJpa;
import com.atlashub.pay.ledger.infrastructure.persistence.entities.LedgerTransactionJpa;
import com.atlashub.pay.ledger.infrastructure.persistence.mappers.LedgerEntryMapper;
import com.atlashub.pay.ledger.infrastructure.persistence.mappers.LedgerTransactionMapper;
import com.atlashub.pay.ledger.infrastructure.persistence.repositories.SpringDataLedgerEntryRepository;
import com.atlashub.pay.ledger.infrastructure.persistence.repositories.SpringDataLedgerTransactionRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.domain.event.DomainEvent;
import com.atlashub.shared.domain.event.EnvelopedDomainEvent;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class LedgerTransactionRepositoryAdapter implements LedgerTransactionRepository {

    private final SpringDataLedgerTransactionRepository txRepo;
    private final SpringDataLedgerEntryRepository entryRepo;
    private final LedgerTransactionMapper txMapper;
    private final LedgerEntryMapper entryMapper;
    private final DomainSequenceGenerator sequenceGenerator;
    private final DomainEventPublisher eventPublisher;

    public LedgerTransactionRepositoryAdapter(SpringDataLedgerTransactionRepository txRepo,
                                              SpringDataLedgerEntryRepository entryRepo,
                                              LedgerTransactionMapper txMapper,
                                              LedgerEntryMapper entryMapper,
                                              DomainSequenceGenerator sequenceGenerator,
                                              DomainEventPublisher eventPublisher) {
        this.txRepo = txRepo;
        this.entryRepo = entryRepo;
        this.txMapper = txMapper;
        this.entryMapper = entryMapper;
        this.sequenceGenerator = sequenceGenerator;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Long nextIdentity() {
        return sequenceGenerator.nextIdentity("ledger_transaction_seq");
    }

    @Override
    @Transactional
    public LedgerTransaction save(LedgerTransaction entity) {
        LedgerTransactionJpa record = txMapper.toPersistence(entity);
        List<LedgerEntryJpa> entryRecords = entity.getEntries().stream()
                .map(entryMapper::toPersistence)
                .collect(Collectors.toList());

        List<DomainEvent<?>> events = entity.pullDomainEvents();
        if (events != null) {
            for (DomainEvent<?> event : events) {
                eventPublisher.publish(EnvelopedDomainEvent.wrap(event));
            }
        }

        LedgerTransactionJpa savedTx = txRepo.save(record);
        List<LedgerEntryJpa> savedEntries = entryRepo.saveAll(entryRecords);

        return txMapper.toDomain(savedTx, savedEntries.stream().map(entryMapper::toDomain).collect(Collectors.toList()));
    }

    @Override
    public Optional<LedgerTransaction> findById(Long id) {
        return txRepo.findById(id).map(tx -> {
            List<LedgerEntryJpa> entries = entryRepo.findByTransactionId(id);
            return txMapper.toDomain(tx, entries.stream().map(entryMapper::toDomain).collect(Collectors.toList()));
        });
    }

    @Override
    public void deleteById(Long id) {
        entryRepo.deleteAll(entryRepo.findByTransactionId(id));
        txRepo.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return txRepo.existsById(id);
    }

    @Override
    public Optional<LedgerTransaction> findByReference(String reference) {
        return txRepo.findByReference(reference).map(tx -> {
            List<LedgerEntryJpa> entries = entryRepo.findByTransactionId(tx.getId());
            return txMapper.toDomain(tx, entries.stream().map(entryMapper::toDomain).collect(Collectors.toList()));
        });
    }

    @Override
    public List<LedgerTransaction> findByOrganizationId(Long organizationId, Pageable pageable) {
        List<LedgerTransactionJpa> txs = txRepo.findByOrganizationId(organizationId, pageable);
        List<Long> txIds = txs.stream().map(LedgerTransactionJpa::getId).collect(Collectors.toList());
        List<LedgerEntryJpa> allEntries = entryRepo.findByTransactionIdIn(txIds);
        
        return txs.stream().map(tx -> {
            List<LedgerEntryJpa> entries = allEntries.stream()
                    .filter(e -> e.getTransactionId().equals(tx.getId()))
                    .toList();
            return txMapper.toDomain(tx, entries.stream().map(entryMapper::toDomain).collect(Collectors.toList()));
        }).collect(Collectors.toList());
    }

    @Override
    public Page<LedgerTransaction> findHistory(Long organizationId, Long accountId, LocalDate dateFrom, LocalDate dateTo, Pageable pageable) {
        ZonedDateTime zDateFrom = dateFrom != null ? dateFrom.atStartOfDay(ZoneOffset.UTC) : null;
        ZonedDateTime zDateTo = dateTo != null ? dateTo.plusDays(1).atStartOfDay(ZoneOffset.UTC).minusNanos(1) : null;
        
        Page<LedgerTransactionJpa> txPage = txRepo.findHistory(organizationId, accountId, zDateFrom, zDateTo, pageable);
        List<Long> txIds = txPage.getContent().stream().map(LedgerTransactionJpa::getId).collect(Collectors.toList());
        List<LedgerEntryJpa> allEntries = entryRepo.findByTransactionIdIn(txIds);

        return txPage.map(tx -> {
            List<LedgerEntryJpa> entries = allEntries.stream()
                    .filter(e -> e.getTransactionId().equals(tx.getId()))
                    .toList();
            return txMapper.toDomain(tx, entries.stream().map(entryMapper::toDomain).collect(Collectors.toList()));
        });
    }

    @Override
    public List<LedgerTransaction> findByAccountIdAndPostedAtAfter(Long accountId, ZonedDateTime postedAt) {
        List<LedgerTransactionJpa> txs = txRepo.findByAccountIdAndPostedAtAfter(accountId, postedAt);
        List<Long> txIds = txs.stream().map(LedgerTransactionJpa::getId).collect(Collectors.toList());
        List<LedgerEntryJpa> allEntries = entryRepo.findByTransactionIdIn(txIds);
        
        return txs.stream().map(tx -> {
            List<LedgerEntryJpa> entries = allEntries.stream()
                    .filter(e -> e.getTransactionId().equals(tx.getId()))
                    .toList();
            return txMapper.toDomain(tx, entries.stream().map(entryMapper::toDomain).collect(Collectors.toList()));
        }).collect(Collectors.toList());
    }
}
