package com.atlashub.identity.application.usecase;

import org.springframework.stereotype.Service;

import com.atlashub.identity.application.command.RegisterOrganizationCommand;
import com.atlashub.shared.usecase.BaseUseCase;
import com.atlashub.identity.application.dto.RegisterOrganizationResult;
import com.atlashub.identity.domain.exception.IdentityErrorCode;
import com.atlashub.identity.domain.model.Organization;
import com.atlashub.identity.domain.repository.OrganizationRepository;
import com.atlashub.shared.domain.valueobject.Country;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import com.atlashub.shared.domain.valueobject.PhoneNumber;
import com.atlashub.shared.event.DomainEventPublisher;
import com.atlashub.shared.exception.ConflictException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterOrganizationUseCase extends BaseUseCase<RegisterOrganizationCommand, RegisterOrganizationResult> {

    private static final Logger log = LoggerFactory.getLogger(RegisterOrganizationUseCase.class);

    private final OrganizationRepository OrganizationRepository;
    private final DomainEventPublisher eventPublisher;

    public RegisterOrganizationUseCase(
            OrganizationRepository OrganizationRepository,
            DomainEventPublisher eventPublisher) {
        this.OrganizationRepository = OrganizationRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public RegisterOrganizationResult execute(RegisterOrganizationCommand command) {
        log.info("Starting Organization registration for email: {}", command.email());

        if (OrganizationRepository.findByEmail(command.email()).isPresent()) {
            log.warn("Organization registration failed: email {} already exists", command.email());
            throw new ConflictException(IdentityErrorCode.Organization_EMAIL_ALREADY_EXISTS, "Organization with this email already exists");
        }

        Organization Organization = new Organization(OrganizationRepository.nextIdentity(),
            Country.fromString(command.country()),
            command.businessName(),
            command.firstName(),
            command.lastName(),
            new EmailAddress(command.email()),
            new PhoneNumber(command.phone()),
            command.businessType()
        );

        OrganizationRepository.save(Organization);
        log.debug("Organization saved with ID: {}", Organization.getId());

        publishEvents(Organization, eventPublisher);
        log.debug("Organization domain events published");

        log.info("Successfully registered Organization with ID: {}", Organization.getId());

        return new RegisterOrganizationResult(Organization.getId());
    }
}

