package com.atlashub.identity.adapter.in.internal;

import com.atlashub.identity.domain.model.Organization;
import com.atlashub.identity.domain.repository.OrganizationRepository;
import com.atlashub.shared.application.api.OrganizationQueryApi;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class IdentityOrganizationApiImpl implements OrganizationQueryApi {

    private final OrganizationRepository organizationRepository;

    public IdentityOrganizationApiImpl(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    @Override
    public Optional<OrganizationSharedDto> getOrganizationById(Long organizationId) {
        return organizationRepository.findById(organizationId)
            .map(org -> new OrganizationSharedDto(
                org.getId(),
                org.getBusinessName()
            ));
    }
}
