package com.atlashub.charges.application.usecase;

import com.atlashub.charges.application.command.ProcessWalletChargeCommand;
import com.atlashub.accounts.application.port.AccountQueryService;
import com.atlashub.identity.application.port.OrganizationQueryService;
import com.atlashub.identity.application.port.OrganizationQueryService.OrganizationSharedDto;
import com.atlashub.charges.domain.model.WalletCharge;
import com.atlashub.charges.domain.repository.WalletChargeRepository;
import com.atlashub.charges.domain.valueobject.ChargePurpose;
import com.atlashub.shared.application.usecase.BaseUseCase;
import com.atlashub.shared.domain.event.DomainEventPublisher;
import com.atlashub.shared.domain.exception.BusinessRuleException;
import com.atlashub.shared.domain.exception.SharedErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProcessWalletChargeUseCase extends BaseUseCase<ProcessWalletChargeCommand, Void> {

    private final WalletChargeRepository walletChargeRepository;
    private final OrganizationQueryService organizationQueryService;
    private final AccountQueryService accountQueryService;
    private final DomainEventPublisher publisher;

    public ProcessWalletChargeUseCase(WalletChargeRepository walletChargeRepository,
                                      OrganizationQueryService organizationQueryService,
                                      AccountQueryService accountQueryService,
                                      DomainEventPublisher publisher) {
        this.walletChargeRepository = walletChargeRepository;
        this.organizationQueryService = organizationQueryService;
        this.accountQueryService = accountQueryService;
        this.publisher = publisher;
    }

    @Override
    @Transactional
    public Void execute(ProcessWalletChargeCommand command) {
        OrganizationSharedDto organization = organizationQueryService.getOrganizationById(Long.valueOf(command.organizationId()))
                .orElseThrow(() -> new BusinessRuleException(SharedErrorCode.NOT_FOUND, "Organization not found"));

        Long sourceAccountId = accountQueryService.getPlatformAccountId("PLATFORM_RECEIVABLE");
        
        Long chargeId = walletChargeRepository.nextIdentity();
        WalletCharge charge = WalletCharge.initiate(
                chargeId,
                Long.valueOf(command.organizationId()),
                sourceAccountId,
                command.amount(),
                command.currency(),
                ChargePurpose.PLATFORM_INVOICE,
                command.invoiceId()
        );

        walletChargeRepository.save(charge);
        publishEvents(charge, publisher);
        return null;
    }
}

