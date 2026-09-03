package com.atlashub.identity.application.usecase;

import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlashub.identity.application.command.CompleteComplianceContactCommand;

import com.atlashub.shared.application.usecase.BaseUseCase;

import com.atlashub.identity.domain.exception.IdentityErrorCode;
import com.atlashub.identity.domain.model.Organization;
import com.atlashub.identity.domain.repository.OrganizationRepository;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import com.atlashub.shared.domain.valueobject.PhoneNumber;
import com.atlashub.shared.application.port.out.DomainEventPublisher;
import com.atlashub.shared.domain.exception.NotFoundException;

@Service
public class CompleteComplianceContactUseCase extends BaseUseCase<CompleteComplianceContactCommand, Void> {
    private static final Logger log = LoggerFactory.getLogger(CompleteComplianceContactUseCase.class);


    private final OrganizationRepository OrganizationRepository;
    private final DomainEventPublisher eventPublisher;

    public CompleteComplianceContactUseCase(OrganizationRepository OrganizationRepository, DomainEventPublisher eventPublisher) {
        this.OrganizationRepository = OrganizationRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public Void execute(CompleteComplianceContactCommand command) {
        log.info("Executing CompleteComplianceContactUseCase");

        Organization Organization = OrganizationRepository.findById(command.OrganizationId())
                .orElseThrow(() -> new NotFoundException(IdentityErrorCode.Organization_NOT_FOUND, "Organization not found"));

        Organization.updateComplianceContact(
            command.supportEmail() != null ? new EmailAddress(command.supportEmail()) : null,
            command.disputeEmail() != null ? new EmailAddress(command.disputeEmail()) : null,
            command.whatsappPhone() != null ? new PhoneNumber(command.whatsappPhone()) : null,
            command.whatsappName(),
            command.websiteUrl(),
            command.twitterHandle(),
            command.facebookUsername(),
            command.instagramHandle(),
            command.businessState(),
            command.businessLga(),
            command.businessCity(),
            command.businessStreet()
        );

        OrganizationRepository.save(Organization);
        publishEvents(Organization, eventPublisher);
    
        return null;
    }
}



