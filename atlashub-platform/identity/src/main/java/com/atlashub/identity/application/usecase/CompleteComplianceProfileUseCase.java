package com.atlashub.identity.application.usecase;

import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlashub.identity.application.command.CompleteComplianceProfileCommand;

import com.atlashub.shared.usecase.BaseUseCase;

import com.atlashub.identity.domain.exception.IdentityErrorCode;
import com.atlashub.identity.domain.model.Organization;
import com.atlashub.identity.domain.repository.OrganizationRepository;
import com.atlashub.shared.event.DomainEventPublisher;
import com.atlashub.shared.exception.NotFoundException;

@Service
public class CompleteComplianceProfileUseCase extends BaseUseCase<CompleteComplianceProfileCommand, Void> {
    private static final Logger log = LoggerFactory.getLogger(CompleteComplianceProfileUseCase.class);


    private final OrganizationRepository OrganizationRepository;
    private final DomainEventPublisher eventPublisher;

    public CompleteComplianceProfileUseCase(OrganizationRepository OrganizationRepository, DomainEventPublisher eventPublisher) {
        this.OrganizationRepository = OrganizationRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public Void execute(CompleteComplianceProfileCommand command) {
        log.info("Executing CompleteComplianceProfileUseCase");

        Organization Organization = OrganizationRepository.findById(command.OrganizationId())
                .orElseThrow(() -> new NotFoundException(IdentityErrorCode.Organization_NOT_FOUND, "Organization not found"));

        Organization.updateComplianceProfile(
            command.description(),
            command.staffSize(),
            command.industry(),
            command.category(),
            command.annualProjectedSalesVolume(),
            command.annualProjectedSalesCurrency()
        );

        OrganizationRepository.save(Organization);
        publishEvents(Organization, eventPublisher);
    
        return null;
    }
}



