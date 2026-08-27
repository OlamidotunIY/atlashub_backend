package com.atlashub.identity.adapter.out.persistence;

import com.atlashub.identity.adapter.out.persistence.mapper.OrganizationMemberMapper;
import com.atlashub.identity.adapter.out.persistence.repository.SpringDataOrganizationMemberRepository;
import com.atlashub.identity.domain.model.OrganizationMember;
import com.atlashub.identity.domain.repository.OrganizationMemberRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class OrganizationMemberPersistenceAdapter implements OrganizationMemberRepository {

    private final SpringDataOrganizationMemberRepository repository;
    private final OrganizationMemberMapper mapper;

    public OrganizationMemberPersistenceAdapter(SpringDataOrganizationMemberRepository repository, OrganizationMemberMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public OrganizationMember save(OrganizationMember member) {
        return mapper.toDomain(repository.save(mapper.toEntity(member)));
    }

    @Override
    public Optional<OrganizationMember> findByOrganizationIdAndUserId(Long organizationId, Long userId) {
        return repository.findByOrganizationIdAndUserId(organizationId, userId)
                .map(mapper::toDomain);
    }

    @Override
    public List<OrganizationMember> findByOrganizationId(Long organizationId) {
        return repository.findByOrganizationId(organizationId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrganizationMember> findByUserId(Long userId) {
        return repository.findByUserId(userId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
