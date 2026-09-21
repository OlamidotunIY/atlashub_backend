package com.atlashub.accounts.application.command.UpdateOrganizationDetails;

import com.atlashub.accounts.domain.exception.OrganizationNotFoundException;
import com.atlashub.accounts.domain.model.Organization;
import com.atlashub.accounts.domain.repository.OrganizationRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.application.usecase.BaseUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

public class UpdateOrganizationDetailsHandler extends BaseUseCase<UpdateOrganizationDetailsCommand, UpdateOrganizationDetailsResult> {

    private static final Logger log = LoggerFactory.getLogger(UpdateOrganizationDetailsHandler.class);
    private final OrganizationRepository organizationRepository;
    private final DomainEventPublisher eventPublisher;

    public UpdateOrganizationDetailsHandler(OrganizationRepository organizationRepository, DomainEventPublisher eventPublisher) {
        this.organizationRepository = organizationRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public UpdateOrganizationDetailsResult execute(UpdateOrganizationDetailsCommand input) {
        log.info("Updating organization profile....");

        Organization organization = organizationRepository.findById(input.organizationId()).orElseThrow(() -> new OrganizationNotFoundException("Organization with id %d is not found".formatted(input.organizationId())));

        organization.updateOrganization(input.businessName(), input.description(), input.logoUrl(), input.industry(), input.websiteUrl());

        organizationRepository.save(organization);

        log.info("Organization ({}) is updated successfully", organization.getBusinessName());

        publishEvents(organization, eventPublisher);

        return new UpdateOrganizationDetailsResult(
                organization
        );
    }
}
