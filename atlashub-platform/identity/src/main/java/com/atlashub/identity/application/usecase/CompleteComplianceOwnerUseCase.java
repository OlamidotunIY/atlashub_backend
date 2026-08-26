package com.atlashub.identity.application.usecase;

import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlashub.identity.application.command.CompleteComplianceOwnerCommand;

import com.atlashub.shared.usecase.BaseUseCase;

import com.atlashub.identity.domain.exception.IdentityErrorCode;
import com.atlashub.identity.domain.model.Organization;
import com.atlashub.identity.domain.repository.OrganizationRepository;
import com.atlashub.shared.event.DomainEventPublisher;
import com.atlashub.shared.exception.NotFoundException;

@Service
public class CompleteComplianceOwnerUseCase extends BaseUseCase<CompleteComplianceOwnerCommand, Void> {
    private static final Logger log = LoggerFactory.getLogger(CompleteComplianceOwnerUseCase.class);


    private final OrganizationRepository OrganizationRepository;
    private final DomainEventPublisher eventPublisher;

    public CompleteComplianceOwnerUseCase(OrganizationRepository OrganizationRepository, DomainEventPublisher eventPublisher) {
        this.OrganizationRepository = OrganizationRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public Void execute(CompleteComplianceOwnerCommand command) {
        log.info("Executing CompleteComplianceOwnerUseCase");

        Organization Organization = OrganizationRepository.findById(command.OrganizationId())
                .orElseThrow(() -> new NotFoundException(IdentityErrorCode.Organization_NOT_FOUND, "Organization not found"));

        Organization.updateComplianceOwner(
            command.ownerBvn(),
            command.ownerNin(),
            command.ownerDateOfBirth(),
            command.ownerAddress(),
            command.ownerIdType(),
            command.ownerIdNumber(),
            command.rcNumber()
        );

        OrganizationRepository.save(Organization);
        publishEvents(Organization, eventPublisher);
    
        return null;
    }
}



