package com.atlashub.accounting.gl.infrastructure.persistence.adapters;

import com.atlashub.accounting.gl.domain.entities.JournalEntry;
import com.atlashub.accounting.gl.domain.repositories.JournalEntryRepository;
import com.atlashub.accounting.gl.domain.valueobject.JournalEntryStatus;
import com.atlashub.accounting.gl.infrastructure.persistence.entities.JournalEntryJpa;
import com.atlashub.accounting.gl.infrastructure.persistence.entities.JournalLineJpa;
import com.atlashub.accounting.gl.infrastructure.persistence.mappers.JournalEntryMapper;
import com.atlashub.accounting.gl.infrastructure.persistence.mappers.JournalLineMapper;
import com.atlashub.accounting.gl.infrastructure.persistence.repositories.SpringDataJournalEntryRepository;
import com.atlashub.accounting.gl.infrastructure.persistence.repositories.SpringDataJournalLineRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.domain.event.DomainEvent;
import com.atlashub.shared.domain.event.EnvelopedDomainEvent;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class JournalEntryRepositoryAdapter
        extends JpaBaseRepository<JournalEntry, JournalEntryJpa>
        implements JournalEntryRepository {

    private final SpringDataJournalEntryRepository entryRepo;
    private final SpringDataJournalLineRepository lineRepo;
    private final JournalEntryMapper entryMapper;
    private final JournalLineMapper lineMapper;

    public JournalEntryRepositoryAdapter(SpringDataJournalEntryRepository entryRepo,
                                         SpringDataJournalLineRepository lineRepo,
                                         JournalEntryMapper mapper,
                                         JournalLineMapper lineMapper,
                                         DomainSequenceGenerator sequenceGenerator,
                                         DomainEventPublisher eventPublisher) {
        super(entryRepo, mapper, sequenceGenerator, eventPublisher);
        this.entryRepo = entryRepo;
        this.lineRepo = lineRepo;
        this.entryMapper = mapper;
        this.lineMapper = lineMapper;
    }

    @Override
    protected String getSequenceName() {
        return "accounting_journal_entry_seq";
    }

    @Override
    public Optional<JournalEntry> findById(Long id) {
        return entryRepo.findById(id).map(this::toFullDomain);
    }

    @Override
    public List<JournalEntry> findAll() {
        return entryRepo.findAll().stream()
                .map(this::toFullDomain)
                .toList();
    }

    @Override
    public Optional<JournalEntry> findByEntryNumber(String entryNumber) {
        return entryRepo.findByEntryNumber(entryNumber).map(this::toFullDomain);
    }

    @Override
    public List<JournalEntry> findByOrganizationId(Long organizationId) {
        return entryRepo.findByOrganizationId(organizationId).stream()
                .map(this::toFullDomain)
                .toList();
    }

    @Override
    public List<JournalEntry> findByOrganizationIdAndStatus(Long organizationId, JournalEntryStatus status) {
        return entryRepo.findByOrganizationIdAndStatus(organizationId, status).stream()
                .map(this::toFullDomain)
                .toList();
    }

    @Override
    public List<JournalEntry> findByOrganizationIdAndDateBetween(Long organizationId, LocalDate from, LocalDate to) {
        return entryRepo.findByOrganizationIdAndDateBetween(organizationId, from, to).stream()
                .map(this::toFullDomain)
                .toList();
    }

    @Override
    @Transactional
    public JournalEntry save(JournalEntry entity) {
        JournalEntryJpa record = entryRepo.findById(entity.getId())
                .map(existing -> {
                    entryMapper.updatePersistence(entity, existing);
                    return existing;
                })
                .orElseGet(() -> entryRepo.save(entryMapper.toPersistence(entity)));

        lineRepo.deleteAllByJournalEntryId(entity.getId());
        List<JournalLineJpa> lineJpas = entity.getLines().stream()
                .map(lineMapper::toPersistence)
                .toList();
        lineRepo.saveAll(lineJpas);

        List<DomainEvent<?>> events = entity.pullDomainEvents();
        for (DomainEvent<?> event : events) {
            eventPublisher.publish(EnvelopedDomainEvent.wrap(event));
        }

        return entryMapper.toDomain(record, lineJpas);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        lineRepo.deleteAllByJournalEntryId(id);
        entryRepo.deleteById(id);
    }

    private JournalEntry toFullDomain(JournalEntryJpa record) {
        List<JournalLineJpa> lines = lineRepo.findAllByJournalEntryId(record.getId());
        return entryMapper.toDomain(record, lines);
    }
}
