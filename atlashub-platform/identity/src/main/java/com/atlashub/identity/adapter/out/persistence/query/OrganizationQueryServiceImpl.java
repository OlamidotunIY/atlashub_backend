package com.atlashub.identity.adapter.out.persistence.query;

import com.atlashub.identity.application.result.OrganizationProfileDto;
import com.atlashub.identity.application.port.OrganizationQueryService;
import com.atlashub.identity.adapter.out.persistence.repository.SpringDataOrganizationRepository;
import com.atlashub.shared.application.util.PageResult;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class OrganizationQueryServiceImpl implements OrganizationQueryService {

    private final SpringDataOrganizationRepository repository;

    public OrganizationQueryServiceImpl(SpringDataOrganizationRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<OrganizationProfileDto> getProfile(Long organizationId) {
        return repository.findById(organizationId)
                .map(entity -> new OrganizationProfileDto(
                        entity.getId(),
                        entity.getBusinessName(),
                        entity.getDescription(),
                        entity.getLogoUrl(),
                        entity.getComplianceStatus().name(),
                        entity.getCreatedAt()
                ));
    }

    @Override
    public PageResult<OrganizationProfileDto> findAll(int page, int size, String searchFilter) {
        return new PageResult<>(Collections.emptyList(), page, size, 0L, 0);
    }

    @Override
    public Optional<OrganizationSharedDto> getOrganizationById(Long organizationId) {
        return repository.findById(organizationId)
                .map(org -> new OrganizationSharedDto(
                        org.getId(),
                        org.getBusinessName(),
                        org.getCurrency()
                ));
    }
}
