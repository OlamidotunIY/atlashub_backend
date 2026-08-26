package com.atlashub.identity.adapter.out.persistence.adapter;

import com.atlashub.identity.domain.model.Organization;
import com.atlashub.identity.domain.repository.OrganizationRepository;
import com.atlashub.identity.adapter.out.persistence.entity.OrganizationJpaEntity;
import com.atlashub.identity.adapter.out.persistence.mapper.OrganizationMapper;
import com.atlashub.identity.adapter.out.persistence.repository.SpringDataOrganizationRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class OrganizationRepositoryAdapter implements OrganizationRepository {

    private final SpringDataOrganizationRepository jpaRepository;
    private final com.atlashub.shared.adapter.out.external.DomainSequenceGenerator sequenceGenerator;
    private final OrganizationMapper mapper;

    public OrganizationRepositoryAdapter(SpringDataOrganizationRepository jpaRepository, com.atlashub.shared.adapter.out.external.DomainSequenceGenerator sequenceGenerator, OrganizationMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.sequenceGenerator = sequenceGenerator;
        this.mapper = mapper;
    }

    @Override
    public Organization save(Organization Organization) {
        OrganizationJpaEntity entity = mapper.toEntity(Organization);
        jpaRepository.save(entity);
        return Organization;
    }

    @Override
    public Optional<Organization> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Organization> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public Long nextIdentity() {
        return sequenceGenerator.nextIdentity("Organization_seq");
    }


}
