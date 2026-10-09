package com.atlashub.compliance.infrastructure.persistence.adapters;

import com.atlashub.compliance.domain.entities.ComplianceRecord;
import com.atlashub.compliance.domain.repositories.ComplianceRecordRepository;
import com.atlashub.compliance.domain.valueobject.ComplianceStatus;
import com.atlashub.compliance.infrastructure.persistence.entities.ComplianceRecordJpa;
import com.atlashub.compliance.infrastructure.persistence.mappers.ComplianceRecordMapper;
import com.atlashub.compliance.infrastructure.persistence.repositories.SpringDataComplianceRecordRepository;
import com.atlashub.compliance.infrastructure.persistence.repositories.SpringDataBusinessOfficerRepository;
import com.atlashub.compliance.infrastructure.persistence.repositories.SpringDataComplianceDocumentRequirementRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.domain.valueobject.PageResult;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ComplianceRecordRepositoryAdapter extends JpaBaseRepository<ComplianceRecord, ComplianceRecordJpa> implements ComplianceRecordRepository {

    private final SpringDataComplianceRecordRepository repository;
    private final SpringDataBusinessOfficerRepository officers;
    private final SpringDataComplianceDocumentRequirementRepository documentRequirements;
    private final ComplianceRecordMapper complianceMapper;

    public ComplianceRecordRepositoryAdapter(
            SpringDataComplianceRecordRepository springDataRepository,
            ComplianceRecordMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher,
            SpringDataBusinessOfficerRepository officers,
            SpringDataComplianceDocumentRequirementRepository documentRequirements) {
        super(springDataRepository, mapper, sequenceGenerator, eventPublisher);
        this.repository = springDataRepository;
        this.officers = officers;
        this.documentRequirements = documentRequirements;
        this.complianceMapper = mapper;
    }

    @Override
    protected String getSequenceName() {
        return "compliance_record_seq";
    }

    @Override
    public Optional<ComplianceRecord> findByOrganizationId(Long organizationId) {
        return repository.findByOrganizationId(organizationId).map(this::toDomain);
    }

    @Override
    public Optional<ComplianceRecord> findByAnchorBusinessCustomerId(String anchorBusinessCustomerId) {
        return repository.findByAnchorBusinessCustomerId(anchorBusinessCustomerId).map(this::toDomain);
    }

    @Override
    public Optional<ComplianceRecord> findByAnchorDocumentId(String anchorDocumentId) {
        return documentRequirements.findByAnchorDocumentId(anchorDocumentId)
                .flatMap(row -> repository.findById(row.getComplianceRecordId()))
                .map(this::toDomain);
    }

    @Override
    public PageResult<ComplianceRecord> findAllByStatus(ComplianceStatus status, int page, int size) {
        Page<ComplianceRecordJpa> jpaPage = repository.findAllByStatus(status, PageRequest.of(page, size));
        List<ComplianceRecord> content = jpaPage.getContent().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
        return new PageResult<>(
                content,
                jpaPage.getNumber(),
                jpaPage.getSize(),
                jpaPage.getTotalElements(),
                jpaPage.getTotalPages()
        );
    }

    @Override
    public Optional<ComplianceRecord> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<ComplianceRecord> findAll() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional
    public ComplianceRecord save(ComplianceRecord entity) {
        ComplianceRecordJpa row = repository.findById(entity.getId())
                .map(existing -> {
                    complianceMapper.updatePersistence(entity, existing);
                    return existing;
                })
                .orElseGet(() -> repository.save(complianceMapper.toPersistence(entity)));
        officers.deleteAllByComplianceRecordId(entity.getId());
        documentRequirements.deleteAllByComplianceRecordId(entity.getId());
        officers.saveAll(entity.getOfficers().stream()
                .map(officer -> complianceMapper.toPersistence(entity.getId(), officer)).toList());
        documentRequirements.saveAll(entity.getDocumentRequirements().stream()
                .map(document -> complianceMapper.toPersistence(entity.getId(), document)).toList());
        entity.pullDomainEvents().forEach(event -> eventPublisher.publish(
                com.atlashub.shared.domain.event.EnvelopedDomainEvent.wrap(event)));
        return toDomain(row);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        officers.deleteAllByComplianceRecordId(id);
        documentRequirements.deleteAllByComplianceRecordId(id);
        repository.deleteById(id);
    }

    private ComplianceRecord toDomain(ComplianceRecordJpa row) {
        return complianceMapper.toDomain(row, officers.findAllByComplianceRecordId(row.getId()),
                documentRequirements.findAllByComplianceRecordId(row.getId()));
    }
}
