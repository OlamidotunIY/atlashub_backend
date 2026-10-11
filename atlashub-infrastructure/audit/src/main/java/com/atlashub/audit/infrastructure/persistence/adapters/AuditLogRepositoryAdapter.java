package com.atlashub.audit.infrastructure.persistence.adapters;

import com.atlashub.audit.domain.entities.AuditLog;
import com.atlashub.audit.domain.repositories.AuditLogRepository;
import com.atlashub.audit.infrastructure.persistence.entities.AuditLogJpa;
import com.atlashub.audit.infrastructure.persistence.mappers.AuditLogMapper;
import com.atlashub.audit.infrastructure.persistence.repositories.SpringDataAuditLogRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuditLogRepositoryAdapter
        extends JpaBaseRepository<AuditLog, AuditLogJpa>
        implements AuditLogRepository {

    private final SpringDataAuditLogRepository springDataRepo;

    public AuditLogRepositoryAdapter(SpringDataAuditLogRepository springDataRepo,
                                     AuditLogMapper mapper,
                                     DomainSequenceGenerator sequenceGenerator,
                                     DomainEventPublisher eventPublisher) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "audit_log_seq";
    }

    @Override
    public List<AuditLog> findByOrganizationId(Long organizationId, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size);
        return springDataRepo.findByOrganizationIdOrderByTimestampDesc(organizationId, pageable)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<AuditLog> findByOrganizationIdAndResourceType(Long organizationId, String resourceType, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size);
        return springDataRepo.findByOrganizationIdAndResourceTypeOrderByTimestampDesc(organizationId, resourceType, pageable)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
