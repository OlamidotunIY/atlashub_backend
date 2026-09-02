package com.atlashub.identity.application.usecase;

import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlashub.identity.application.command.CompleteComplianceAccountCommand;
import com.atlashub.identity.application.port.AccountNameResolutionPort;
import com.atlashub.identity.domain.valueobject.ComplianceStatus;
import com.atlashub.identity.domain.model.Organization;
import com.atlashub.identity.domain.repository.OrganizationRepository;
import com.atlashub.shared.application.port.out.DomainEventPublisher;
import com.atlashub.shared.domain.exception.NotFoundException;
import com.atlashub.identity.domain.exception.IdentityErrorCode;
import com.atlashub.shared.application.usecase.BaseUseCase;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Service
public class CompleteComplianceAccountUseCase extends BaseUseCase<CompleteComplianceAccountCommand, ComplianceStatus> {
    private static final Logger log = LoggerFactory.getLogger(CompleteComplianceAccountUseCase.class);


    private final OrganizationRepository OrganizationRepository;
    private final AccountNameResolutionPort accountNameResolutionPort;
    private final DomainEventPublisher eventPublisher;

    public CompleteComplianceAccountUseCase(
            OrganizationRepository OrganizationRepository,
            AccountNameResolutionPort accountNameResolutionPort,
            DomainEventPublisher eventPublisher) {
        this.OrganizationRepository = OrganizationRepository;
        this.accountNameResolutionPort = accountNameResolutionPort;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public ComplianceStatus execute(CompleteComplianceAccountCommand command) {
        log.info("Executing CompleteComplianceAccountUseCase");

        Organization Organization = OrganizationRepository.findById(command.OrganizationId())
                .orElseThrow(() -> new NotFoundException(IdentityErrorCode.Organization_NOT_FOUND, "Organization not found"));

        String accountName = accountNameResolutionPort.resolve(command.settlementBankCode(), command.settlementAccountNumber());

        Organization.updateComplianceAccount(
            command.settlementBankCode(),
            command.settlementAccountNumber(),
            accountName
        );

        OrganizationRepository.save(Organization);
        publishEvents(Organization, eventPublisher);

        return Organization.getComplianceStatus();
    }
}




