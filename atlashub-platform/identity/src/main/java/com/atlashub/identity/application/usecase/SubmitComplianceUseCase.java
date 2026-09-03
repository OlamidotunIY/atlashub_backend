package com.atlashub.identity.application.usecase;

import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlashub.identity.application.command.SubmitComplianceCommand;

import com.atlashub.shared.application.usecase.BaseUseCase;

import com.atlashub.identity.domain.exception.IdentityErrorCode;
import com.atlashub.identity.domain.model.Organization;
import com.atlashub.identity.domain.repository.OrganizationRepository;
import com.atlashub.shared.application.port.out.DomainEventPublisher;
import com.atlashub.shared.domain.exception.NotFoundException;

@Service
public class SubmitComplianceUseCase extends BaseUseCase<SubmitComplianceCommand, Void> {
    private static final Logger log = LoggerFactory.getLogger(SubmitComplianceUseCase.class);


    private final OrganizationRepository OrganizationRepository;
    private final DomainEventPublisher eventPublisher;

    public SubmitComplianceUseCase(OrganizationRepository OrganizationRepository, DomainEventPublisher eventPublisher) {
        this.OrganizationRepository = OrganizationRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public Void execute(SubmitComplianceCommand command) {
        log.info("Executing SubmitComplianceUseCase");

        Organization Organization = OrganizationRepository.findById(command.OrganizationId())
                .orElseThrow(() -> new NotFoundException(IdentityErrorCode.Organization_NOT_FOUND, "Organization not found"));

        Organization.submitCompliance();

        OrganizationRepository.save(Organization);
        publishEvents(Organization, eventPublisher);
    
        return null;
    }
}



