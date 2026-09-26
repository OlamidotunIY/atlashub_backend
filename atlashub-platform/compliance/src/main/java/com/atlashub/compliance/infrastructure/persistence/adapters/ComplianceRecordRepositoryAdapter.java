package com.atlashub.compliance.infrastructure.persistence.adapters;

import com.atlashub.compliance.domain.entities.ComplianceRecord;
import com.atlashub.compliance.domain.repositories.ComplianceRecordRepository;
import com.atlashub.compliance.domain.valueobject.ComplianceStatus;
import com.atlashub.compliance.infrastructure.persistence.entities.ComplianceRecordJpa;
import com.atlashub.compliance.infrastructure.persistence.mappers.ComplianceRecordMapper;
import com.atlashub.compliance.infrastructure.persistence.repositories.SpringDataComplianceRecordRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.domain.valueobject.PageResult;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ComplianceRecordRepositoryAdapter extends JpaBaseRepository<ComplianceRecord, ComplianceRecordJpa> implements ComplianceRecordRepository {

    private final SpringDataComplianceRecordRepository repository;

    public ComplianceRecordRepositoryAdapter(
            SpringDataComplianceRecordRepository springDataRepository,
            ComplianceRecordMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher) {
        super(springDataRepository, mapper, sequenceGenerator, eventPublisher);
        this.repository = springDataRepository;
    }

    @Override
    protected String getSequenceName() {
        return "compliance_record_seq";
    }

    @Override
    public Optional<ComplianceRecord> findByOrganizationId(Long organizationId) {
        return repository.findByOrganizationId(organizationId).map(mapper::toDomain);
    }

    @Override
    public PageResult<ComplianceRecord> findAllByStatus(ComplianceStatus status, int page, int size) {
        Page<ComplianceRecordJpa> jpaPage = repository.findAllByStatus(status, PageRequest.of(page, size));
        List<ComplianceRecord> content = jpaPage.getContent().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
        return new PageResult<>(
                content,
                jpaPage.getNumber(),
                jpaPage.getSize(),
                jpaPage.getTotalElements(),
                jpaPage.getTotalPages()
        );
    }
}
