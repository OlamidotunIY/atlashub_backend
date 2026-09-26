package com.atlashub.iam.infrastructure.persistence.adapters;

import com.atlashub.iam.domain.entities.CustomRole;
import com.atlashub.iam.domain.repositories.CustomRoleRepository;
import com.atlashub.iam.infrastructure.persistence.entities.CustomRoleJpa;
import com.atlashub.iam.infrastructure.persistence.mappers.CustomRoleMapper;
import com.atlashub.iam.infrastructure.persistence.repositories.SpringDataCustomRoleRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CustomRoleRepositoryAdapter 
        extends JpaBaseRepository<CustomRole, CustomRoleJpa> 
        implements CustomRoleRepository {

    private final SpringDataCustomRoleRepository springDataRepo;

    public CustomRoleRepositoryAdapter(SpringDataCustomRoleRepository springDataRepo,
                                       CustomRoleMapper mapper,
                                       DomainSequenceGenerator sequenceGenerator,
                                       DomainEventPublisher eventPublisher) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "custom_role_seq";
    }

    @Override
    public List<CustomRole> findByOrganizationId(Long organizationId) {
        return springDataRepo.findByOrganizationId(organizationId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}

