package com.atlashub.identity.application.usecase;

import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlashub.identity.application.command.CompleteComplianceServiceAgreementCommand;

import com.atlashub.shared.usecase.BaseUseCase;

import com.atlashub.identity.domain.exception.IdentityErrorCode;
import com.atlashub.identity.domain.model.Merchant;
import com.atlashub.identity.domain.repository.MerchantRepository;
import com.atlashub.shared.event.DomainEventPublisher;
import com.atlashub.shared.exception.BusinessRuleException;
import com.atlashub.shared.exception.NotFoundException;

@Service
public class CompleteComplianceServiceAgreementUseCase extends BaseUseCase<CompleteComplianceServiceAgreementCommand, Void> {
    private static final Logger log = LoggerFactory.getLogger(CompleteComplianceServiceAgreementUseCase.class);


    private final MerchantRepository merchantRepository;
    private final DomainEventPublisher eventPublisher;

    public CompleteComplianceServiceAgreementUseCase(MerchantRepository merchantRepository, DomainEventPublisher eventPublisher) {
        this.merchantRepository = merchantRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public Void execute(CompleteComplianceServiceAgreementCommand command) {
        log.info("Executing CompleteComplianceServiceAgreementUseCase");

        if (!command.agreed()) {
            throw new BusinessRuleException(IdentityErrorCode.COMPLIANCE_NOT_ALL_STEPS_COMPLETE, "Must agree to service agreement");
        }

        Merchant merchant = merchantRepository.findById(command.merchantId())
                .orElseThrow(() -> new NotFoundException(IdentityErrorCode.MERCHANT_NOT_FOUND, "Merchant not found"));

        merchant.acceptServiceAgreement();

        merchantRepository.save(merchant);
        publishEvents(merchant, eventPublisher);
    
        return null;
    }
}



