package com.atlashub.identity.application.usecase;

import com.atlashub.identity.application.command.RegisterSplitRecipientCommand;
import com.atlashub.identity.application.port.AccountNameResolutionPort;
import com.atlashub.identity.application.result.RegisterSplitRecipientResult;
import com.atlashub.identity.domain.model.SplitRecipient;
import com.atlashub.identity.domain.repository.SplitRecipientRepository;
import com.atlashub.shared.application.port.out.DomainEventPublisher;
import com.atlashub.shared.application.usecase.BaseUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Component
@Service
public class RegisterSplitRecipientUseCase extends BaseUseCase<RegisterSplitRecipientCommand, RegisterSplitRecipientResult> {
    private static final Logger log = LoggerFactory.getLogger(RegisterSplitRecipientUseCase.class);


    private final SplitRecipientRepository SplitRecipientRepository;
    private final AccountNameResolutionPort accountNameResolutionPort;
    private final DomainEventPublisher eventPublisher;

    public RegisterSplitRecipientUseCase(SplitRecipientRepository SplitRecipientRepository, 
                                     AccountNameResolutionPort accountNameResolutionPort, 
                                     DomainEventPublisher eventPublisher) {
        this.SplitRecipientRepository = SplitRecipientRepository;
        this.accountNameResolutionPort = accountNameResolutionPort;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public RegisterSplitRecipientResult execute(RegisterSplitRecipientCommand input) {
        log.info("Executing RegisterSplitRecipientUseCase");

        String accountName = accountNameResolutionPort.resolve(input.bankCode(), input.accountNumber());
        
        SplitRecipient SplitRecipient = new SplitRecipient(SplitRecipientRepository.nextIdentity(),
                input.OrganizationId(),
                input.bankCode(),
                input.accountNumber(),
                accountName,
                input.description()
        );
        
        SplitRecipientRepository.save(SplitRecipient);
        publishEvents(SplitRecipient, eventPublisher);
        
        return new RegisterSplitRecipientResult(SplitRecipient.getId(), accountName);
    }
}



