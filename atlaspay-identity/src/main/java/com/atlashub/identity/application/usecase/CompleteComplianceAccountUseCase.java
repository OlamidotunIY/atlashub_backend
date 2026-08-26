package com.atlashub.identity.application.usecase;

import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlashub.identity.application.command.CompleteComplianceAccountCommand;
import com.atlashub.identity.application.port.AccountNameResolutionPort;
import com.atlashub.identity.domain.model.ComplianceStatus;
import com.atlashub.identity.domain.model.Merchant;
import com.atlashub.identity.domain.repository.MerchantRepository;
import com.atlashub.shared.event.DomainEventPublisher;
import com.atlashub.shared.exception.NotFoundException;
import com.atlashub.identity.domain.exception.IdentityErrorCode;
import com.atlashub.shared.usecase.BaseUseCase;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Service
public class CompleteComplianceAccountUseCase extends BaseUseCase<CompleteComplianceAccountCommand, ComplianceStatus> {
    private static final Logger log = LoggerFactory.getLogger(CompleteComplianceAccountUseCase.class);


    private final MerchantRepository merchantRepository;
    private final AccountNameResolutionPort accountNameResolutionPort;
    private final DomainEventPublisher eventPublisher;

    public CompleteComplianceAccountUseCase(
            MerchantRepository merchantRepository,
            AccountNameResolutionPort accountNameResolutionPort,
            DomainEventPublisher eventPublisher) {
        this.merchantRepository = merchantRepository;
        this.accountNameResolutionPort = accountNameResolutionPort;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public ComplianceStatus execute(CompleteComplianceAccountCommand command) {
        log.info("Executing CompleteComplianceAccountUseCase");

        Merchant merchant = merchantRepository.findById(command.merchantId())
                .orElseThrow(() -> new NotFoundException(IdentityErrorCode.MERCHANT_NOT_FOUND, "Merchant not found"));

        String accountName = accountNameResolutionPort.resolve(command.settlementBankCode(), command.settlementAccountNumber());

        merchant.updateComplianceAccount(
            command.settlementBankCode(),
            command.settlementAccountNumber(),
            accountName
        );

        merchantRepository.save(merchant);
        publishEvents(merchant, eventPublisher);

        return merchant.getComplianceStatus();
    }
}



