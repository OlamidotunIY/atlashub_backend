package com.atlashub.accounts.application.command.UpdateOrganizationDetails;

import com.atlashub.accounts.domain.exception.OrganizationNotFoundException;
import com.atlashub.accounts.domain.model.Organization;
import com.atlashub.accounts.domain.repository.OrganizationRepository;
import com.atlashub.shared.application.usecase.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UpdateOrganizationDetailsHandler extends Command<UpdateOrganizationDetailsCommand, UpdateOrganizationDetailsResult> {

    private static final Logger log = LoggerFactory.getLogger(UpdateOrganizationDetailsHandler.class);
    private final OrganizationRepository organizationRepository;

    public UpdateOrganizationDetailsHandler(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    @Override
    @Transactional
    public UpdateOrganizationDetailsResult execute(UpdateOrganizationDetailsCommand command) {
        log.info("Updating organization details for id: {}", command.organizationId());

        Organization organization = organizationRepository.findById(command.organizationId())
                .orElseThrow(() -> new OrganizationNotFoundException("Organization not found"));

        organization.updateOrganization(command.businessName(), command.description(),
                command.logoUrl(), command.industry(), command.websiteUrl());
        organizationRepository.save(organization);

        log.info("Successfully updated organization id: {}", organization.getId());
        return new UpdateOrganizationDetailsResult(organization);
    }
}
