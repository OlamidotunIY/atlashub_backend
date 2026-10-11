package com.atlashub.accounting.cash.infrastructure.persistence.adapters;

import com.atlashub.accounting.cash.domain.entities.BankReconciliation;
import com.atlashub.accounting.cash.domain.entities.BankStatementLine;
import com.atlashub.accounting.cash.domain.entities.ReconciliationItem;
import com.atlashub.accounting.cash.domain.repositories.BankReconciliationRepository;
import com.atlashub.accounting.cash.domain.valueobject.ReconciliationStatus;
import com.atlashub.accounting.cash.infrastructure.persistence.entities.BankReconciliationJpaEntity;
import com.atlashub.accounting.cash.infrastructure.persistence.entities.BankStatementLineJpaEntity;
import com.atlashub.accounting.cash.infrastructure.persistence.entities.ReconciliationItemJpaEntity;
import com.atlashub.accounting.cash.infrastructure.persistence.mappers.BankReconciliationMapper;
import com.atlashub.accounting.cash.infrastructure.persistence.mappers.BankStatementLineMapper;
import com.atlashub.accounting.cash.infrastructure.persistence.mappers.ReconciliationItemMapper;
import com.atlashub.accounting.cash.infrastructure.persistence.repositories.SpringDataBankReconciliationRepository;
import com.atlashub.accounting.cash.infrastructure.persistence.repositories.SpringDataBankStatementLineRepository;
import com.atlashub.accounting.cash.infrastructure.persistence.repositories.SpringDataReconciliationItemRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.domain.event.DomainEvent;
import com.atlashub.shared.domain.event.EnvelopedDomainEvent;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class BankReconciliationRepositoryAdapter implements BankReconciliationRepository {

    private static final String SEQUENCE_NAME = "accounting_reconciliation_seq";

    private final SpringDataBankReconciliationRepository reconciliationRepository;
    private final SpringDataBankStatementLineRepository statementLineRepository;
    private final SpringDataReconciliationItemRepository reconciliationItemRepository;
    private final BankReconciliationMapper reconciliationMapper;
    private final BankStatementLineMapper statementLineMapper;
    private final ReconciliationItemMapper reconciliationItemMapper;
    private final DomainSequenceGenerator sequenceGenerator;
    private final DomainEventPublisher eventPublisher;

    public BankReconciliationRepositoryAdapter(
            SpringDataBankReconciliationRepository reconciliationRepository,
            SpringDataBankStatementLineRepository statementLineRepository,
            SpringDataReconciliationItemRepository reconciliationItemRepository,
            BankReconciliationMapper reconciliationMapper,
            BankStatementLineMapper statementLineMapper,
            ReconciliationItemMapper reconciliationItemMapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher) {
        this.reconciliationRepository = reconciliationRepository;
        this.statementLineRepository = statementLineRepository;
        this.reconciliationItemRepository = reconciliationItemRepository;
        this.reconciliationMapper = reconciliationMapper;
        this.statementLineMapper = statementLineMapper;
        this.reconciliationItemMapper = reconciliationItemMapper;
        this.sequenceGenerator = sequenceGenerator;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Long nextIdentity() {
        return sequenceGenerator.nextIdentity(SEQUENCE_NAME);
    }

    @Override
    @Transactional
    public BankReconciliation save(BankReconciliation entity) {
        BankReconciliationJpaEntity record = reconciliationRepository.findById(entity.getId())
                .map(existing -> {
                    reconciliationMapper.updatePersistence(entity, existing);
                    return reconciliationRepository.save(existing);
                })
                .orElseGet(() -> reconciliationRepository.save(reconciliationMapper.toPersistence(entity)));

        List<BankStatementLineJpaEntity> statementLines = entity.getUnmatchedBankItems().stream()
                .map(statementLineMapper::toPersistence)
                .toList();
        statementLineRepository.saveAll(statementLines);

        reconciliationItemRepository.deleteByReconciliationId(entity.getId());
        List<ReconciliationItemJpaEntity> reconciliationItems = entity.getMatchedItems().stream()
                .map(reconciliationItemMapper::toPersistence)
                .toList();
        reconciliationItemRepository.saveAll(reconciliationItems);

        reconciliationRepository.flush();
        statementLineRepository.flush();
        reconciliationItemRepository.flush();

        List<DomainEvent<?>> events = entity.pullDomainEvents();
        for (DomainEvent<?> event : events) {
            eventPublisher.publish(EnvelopedDomainEvent.wrap(event));
        }

        return toFullDomain(record);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BankReconciliation> findById(Long id) {
        return reconciliationRepository.findById(id).map(this::toFullDomain);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        reconciliationItemRepository.deleteByReconciliationId(id);
        statementLineRepository.deleteByReconciliationId(id);
        reconciliationRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return reconciliationRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BankReconciliation> findAll() {
        return reconciliationRepository.findAll().stream().map(this::toFullDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BankReconciliation> findByOrganizationId(Long organizationId) {
        return reconciliationRepository.findByOrganizationIdOrderByCreatedAtDesc(organizationId).stream()
                .map(this::toFullDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BankReconciliation> findByOrganizationIdAndStatus(
            Long organizationId,
            ReconciliationStatus status) {
        return reconciliationRepository
                .findByOrganizationIdAndStatusOrderByCreatedAtDesc(organizationId, status).stream()
                .map(this::toFullDomain)
                .toList();
    }

    private BankReconciliation toFullDomain(BankReconciliationJpaEntity record) {
        List<ReconciliationItem> matchedItems = reconciliationItemRepository
                .findByReconciliationIdOrderById(record.getId()).stream()
                .map(reconciliationItemMapper::toDomain)
                .toList();
        Set<Long> matchedStatementLineIds = new HashSet<>(matchedItems.stream()
                .map(ReconciliationItem::getBankStatementLineId)
                .toList());
        List<BankStatementLine> unmatchedBankItems = statementLineRepository
                .findByReconciliationIdOrderById(record.getId()).stream()
                .filter(line -> !matchedStatementLineIds.contains(line.getId()))
                .map(statementLineMapper::toDomain)
                .toList();
        return reconciliationMapper.toDomain(record, unmatchedBankItems, matchedItems);
    }
}
