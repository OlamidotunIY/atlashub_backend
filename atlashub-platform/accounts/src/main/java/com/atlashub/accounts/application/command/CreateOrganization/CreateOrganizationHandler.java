package com.atlashub.accounts.application.command.CreateOrganization;

import com.atlashub.accounts.domain.entities.Organization;
import com.atlashub.accounts.domain.exceptions.UserNotFoundException;
import com.atlashub.accounts.domain.repositories.OrganizationRepository;
import com.atlashub.accounts.domain.repositories.UserRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CreateOrganizationHandler extends Command<CreateOrganizationCommand, Long> {
    private final OrganizationRepository organizations;
    private final UserRepository users;

    public CreateOrganizationHandler(OrganizationRepository organizations, UserRepository users) {
        this.organizations = organizations;
        this.users = users;
    }

    @Override
    @Transactional
    public Long execute(CreateOrganizationCommand command) {
        var owner = users.findById(command.ownerUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        if (!owner.isEmailVerified()) {
            throw new IllegalStateException("Verified email is required to create an organization");
        }
        Organization organization = Organization.create(organizations.nextIdentity(), command.businessName(),
                command.registrationType(), command.description(), command.baseCurrency(),
                command.logoUrl(), command.country(), command.industry(), command.websiteUrl(), owner.getId());
        organizations.save(organization);
        return organization.getId();
    }
}
