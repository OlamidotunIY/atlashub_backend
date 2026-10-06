package com.atlashub.accounts.application.query.GetOrganizationDetails;

import com.atlashub.accounts.domain.exceptions.OrganizationNotFoundException;
import com.atlashub.accounts.domain.entities.Organization;
import com.atlashub.accounts.domain.repositories.OrganizationRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;
import org.springframework.security.access.prepost.PreAuthorize;

@Component
public class GetOrganizationDetailsHandler extends Query<GetOrganizationDetailsQuery, OrganizationDetailsResult> {

    private final OrganizationRepository organizationRepository;

    public GetOrganizationDetailsHandler(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    @Override
    @PreAuthorize("hasAuthority('accounts:organization:read')")
    public OrganizationDetailsResult execute(GetOrganizationDetailsQuery query) {
        Organization org = organizationRepository.findById(query.orgId()).orElseThrow(() -> new OrganizationNotFoundException("Organization not found"));

        return new OrganizationDetailsResult(org.getId(), org.getBusinessName(), org.getRegistrationType().name(), org.getIndustry().name(), org.getRegistrationDate(), org.getDescription(), org.getLogoUrl(), org.getWebsiteUrl(), org.getCountry().code(), org.getBaseCurrency().name(), org.getCreatedAt());
    }
}
