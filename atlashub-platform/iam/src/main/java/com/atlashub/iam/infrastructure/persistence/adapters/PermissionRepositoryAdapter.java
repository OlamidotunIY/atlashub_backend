package com.atlashub.iam.infrastructure.persistence.adapters;

import com.atlashub.iam.domain.entities.Permission;
import com.atlashub.iam.domain.repositories.PermissionRepository;
import com.atlashub.iam.infrastructure.persistence.entities.PermissionJpa;
import com.atlashub.iam.infrastructure.persistence.mappers.PermissionMapper;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;
import com.atlashub.iam.infrastructure.persistence.repositories.SpringDataPermissionRepository;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class PermissionRepositoryAdapter
        extends JpaBaseRepository<Permission, PermissionJpa>
        implements PermissionRepository {

    private final SpringDataPermissionRepository springDataRepo;

    public PermissionRepositoryAdapter(SpringDataPermissionRepository springDataRepo,
                                       PermissionMapper mapper,
                                       DomainSequenceGenerator sequenceGenerator,
                                       DomainEventPublisher eventPublisher) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "permission_seq";
    }

    @Override
    public List<Permission> findAllById(Iterable<Long> ids) {
        return springDataRepo.findAllById(ids)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Permission> findByModule(String module) {
        return springDataRepo.findByModule(module)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}

