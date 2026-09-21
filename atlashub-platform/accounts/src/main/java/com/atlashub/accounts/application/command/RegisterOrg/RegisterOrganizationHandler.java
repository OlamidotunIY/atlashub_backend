package com.atlashub.accounts.application.command.RegisterOrg;

import com.atlashub.accounts.domain.exception.UserAlreadyExist;
import com.atlashub.accounts.domain.model.Organization;
import com.atlashub.accounts.domain.model.User;
import com.atlashub.accounts.domain.repository.OrganizationRepository;
import com.atlashub.accounts.domain.repository.UserRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.application.port.PasswordEncoderPort;
import com.atlashub.shared.application.usecase.BaseUseCase;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

public class RegisterOrganizationHandler extends BaseUseCase<RegisterOrganizationCommand, RegisterOrganizationResult> {
    private static final Logger log = LoggerFactory.getLogger(RegisterOrganizationHandler.class);

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final DomainEventPublisher eventPublisher;
    private final PasswordEncoderPort passwordEncoderPort;

    public RegisterOrganizationHandler(
            OrganizationRepository organizationRepository,
            UserRepository userRepository,
            DomainEventPublisher eventPublisher, PasswordEncoderPort passwordEncoderPort) {
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
        this.passwordEncoderPort = passwordEncoderPort;
    }

    @Override
    @Transactional
    public RegisterOrganizationResult execute(RegisterOrganizationCommand command) {
        log.info("Starting organization registration for user id: {}", command.userId());

        String hashedPassword = passwordEncoderPort.encode(command.password());

        userRepository.findByEmail(command.email()).orElseThrow(() -> new UserAlreadyExist("User with email: %s already exist".formatted(command.email())));

        User user = User.create(userRepository.nextIdentity(), command.firstName(), command.lastName(), new EmailAddress(command.email()), command.country(), false, hashedPassword);

        userRepository.save(user);
        log.debug("User registered with id: {}", user.getId());

        Organization organization = Organization.create(
                organizationRepository.nextIdentity(),
                command.businessName(),
                command.businessType(),
                command.businessSize(),
                command.description(),
                command.country().deriveCurrency(),
                command.logoUrl(),
                command.country(),
                command.industry(),
                command.websiteUrl()
        );

        organizationRepository.save(organization);
        log.debug("Organization saved with id: {}", organization.getId());

        if (user.getActiveOrganizationId() == null) {
            user.switchActiveOrganization(organization.getId());
            userRepository.save(user);
            publishEvents(user, eventPublisher);
            log.debug("Set activeOrganizationId={} for user {}", organization.getId(), command.userId());
        }

        publishEvents(organization, eventPublisher);
        log.info("Successfully registered organization id: {}", organization.getId());

        return new RegisterOrganizationResult(organization, user);
    }
}
