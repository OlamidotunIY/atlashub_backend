package com.atlashub.accounts.application.command.RegisterOrg;

import com.atlashub.accounts.domain.exceptions.EmailAlreadyExistsException;
import com.atlashub.accounts.domain.entities.Organization;
import com.atlashub.accounts.domain.entities.User;
import com.atlashub.accounts.domain.repositories.OrganizationRepository;
import com.atlashub.accounts.domain.repositories.UserRepository;
import com.atlashub.shared.application.port.OneTimeSecretStore;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Component
public class RegisterOrganizationHandler extends Command<RegisterOrganizationCommand, RegisterOrganizationResult> {
    private static final Logger log = LoggerFactory.getLogger(RegisterOrganizationHandler.class);

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final OneTimeSecretStore oneTimeSecretStore;

    public RegisterOrganizationHandler(OrganizationRepository organizationRepository, UserRepository userRepository, OneTimeSecretStore oneTimeSecretStore) {
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.oneTimeSecretStore = oneTimeSecretStore;
    }

    @Override
    @Transactional
    public RegisterOrganizationResult execute(RegisterOrganizationCommand command) {
        log.info("Starting organization registration for email: {}", command.email());

        if (userRepository.findByEmail(command.email()).isPresent()) {
            throw new EmailAlreadyExistsException("User with email: %s already exists".formatted(command.email()));
        }

        String credentialReference = oneTimeSecretStore.store(command.password(), Duration.ofHours(24));

        User user = User.create(userRepository.nextIdentity(), command.firstName(), command.lastName(), new EmailAddress(command.email()), command.country(), command.isInvited(), command.password(), credentialReference);

        userRepository.save(user);
        log.debug("User registered with id: {}", user.getId());

        Organization organization = Organization.create(
                organizationRepository.nextIdentity(), command.businessName(), command.registrationType(),
                command.description(), command.country().deriveCurrency(),
                command.logoUrl(), command.country(), command.industry(), command.websiteUrl(), user.getId());

        organizationRepository.save(organization);
        log.debug("Organization saved with id: {}", organization.getId());

        // Switch active organization for newly registered user
        user.switchActiveOrganization(organization.getId());
        userRepository.save(user);

        log.info("Successfully registered organization id: {}", organization.getId());
        return new RegisterOrganizationResult(organization, user);
    }
}
