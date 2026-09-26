package com.atlashub.iam.infrastructure.persistence.adapters;

import com.atlashub.iam.domain.entities.OrganizationMember;
import com.atlashub.iam.domain.repositories.OrganizationMemberRepository;
import com.atlashub.iam.domain.valueobject.MemberStatus;
import com.atlashub.iam.infrastructure.persistence.entities.OrganizationMemberJpa;
import com.atlashub.iam.infrastructure.persistence.mappers.OrganizationMemberMapper;
import com.atlashub.iam.infrastructure.persistence.repositories.SpringDataOrganizationMemberRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrganizationMemberRepositoryAdapter
        extends JpaBaseRepository<OrganizationMember, OrganizationMemberJpa>
        implements OrganizationMemberRepository {

    private final SpringDataOrganizationMemberRepository springDataRepo;
    private final OrganizationMemberMapper mapper;

    public OrganizationMemberRepositoryAdapter(SpringDataOrganizationMemberRepository springDataRepo,
                                               OrganizationMemberMapper mapper,
                                               DomainSequenceGenerator sequenceGenerator,
                                               DomainEventPublisher eventPublisher) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
        this.mapper = mapper;
    }

    @Override
    protected String getSequenceName() {
        return "organization_member_seq";
    }

    @Override
    public List<OrganizationMember> findAllByOrganizationId(Long organizationId) {
        return springDataRepo.findAllByOrganizationId(organizationId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrganizationMember> findAllByOrganizationIdAndStatus(Long organizationId, MemberStatus status) {
        return springDataRepo.findAllByOrganizationIdAndStatus(organizationId, status)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}

