package com.atlashub.identity.application.usecase;

import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlashub.identity.application.command.CompleteComplianceContactCommand;

import com.atlashub.shared.usecase.BaseUseCase;

import com.atlashub.identity.domain.exception.IdentityErrorCode;
import com.atlashub.identity.domain.model.Merchant;
import com.atlashub.identity.domain.repository.MerchantRepository;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import com.atlashub.shared.domain.valueobject.PhoneNumber;
import com.atlashub.shared.event.DomainEventPublisher;
import com.atlashub.shared.exception.NotFoundException;

@Service
public class CompleteComplianceContactUseCase extends BaseUseCase<CompleteComplianceContactCommand, Void> {
    private static final Logger log = LoggerFactory.getLogger(CompleteComplianceContactUseCase.class);


    private final MerchantRepository merchantRepository;
    private final DomainEventPublisher eventPublisher;

    public CompleteComplianceContactUseCase(MerchantRepository merchantRepository, DomainEventPublisher eventPublisher) {
        this.merchantRepository = merchantRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public Void execute(CompleteComplianceContactCommand command) {
        log.info("Executing CompleteComplianceContactUseCase");

        Merchant merchant = merchantRepository.findById(command.merchantId())
                .orElseThrow(() -> new NotFoundException(IdentityErrorCode.MERCHANT_NOT_FOUND, "Merchant not found"));

        merchant.updateComplianceContact(
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

        merchantRepository.save(merchant);
        publishEvents(merchant, eventPublisher);
    
        return null;
    }
}



