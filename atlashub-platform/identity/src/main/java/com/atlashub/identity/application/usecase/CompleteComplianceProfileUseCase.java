package com.atlashub.identity.application.usecase;

import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlashub.identity.application.command.CompleteComplianceProfileCommand;

import com.atlashub.shared.usecase.BaseUseCase;

import com.atlashub.identity.domain.exception.IdentityErrorCode;
import com.atlashub.identity.domain.model.Merchant;
import com.atlashub.identity.domain.repository.MerchantRepository;
import com.atlashub.shared.event.DomainEventPublisher;
import com.atlashub.shared.exception.NotFoundException;

@Service
public class CompleteComplianceProfileUseCase extends BaseUseCase<CompleteComplianceProfileCommand, Void> {
    private static final Logger log = LoggerFactory.getLogger(CompleteComplianceProfileUseCase.class);


    private final MerchantRepository merchantRepository;
    private final DomainEventPublisher eventPublisher;

    public CompleteComplianceProfileUseCase(MerchantRepository merchantRepository, DomainEventPublisher eventPublisher) {
        this.merchantRepository = merchantRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public Void execute(CompleteComplianceProfileCommand command) {
        log.info("Executing CompleteComplianceProfileUseCase");

        Merchant merchant = merchantRepository.findById(command.merchantId())
                .orElseThrow(() -> new NotFoundException(IdentityErrorCode.MERCHANT_NOT_FOUND, "Merchant not found"));

        merchant.updateComplianceProfile(
            command.description(),
            command.staffSize(),
            command.industry(),
            command.category(),
            command.annualProjectedSalesVolume(),
            command.annualProjectedSalesCurrency()
        );

        merchantRepository.save(merchant);
        publishEvents(merchant, eventPublisher);
    
        return null;
    }
}



