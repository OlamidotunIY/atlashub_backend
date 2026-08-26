package com.atlashub.identity.application.usecase;

import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlashub.identity.application.command.RegisterSubAccountCommand;
import com.atlashub.identity.application.dto.SubAccountDto;
import com.atlashub.identity.application.port.AccountNameResolutionPort;
import com.atlashub.identity.domain.model.SubAccount;
import com.atlashub.identity.domain.repository.SubAccountRepository;
import com.atlashub.shared.event.DomainEventPublisher;
import com.atlashub.shared.usecase.BaseUseCase;
import org.springframework.stereotype.Component;

import com.atlashub.identity.application.dto.RegisterSubAccountResult;

@Component
@Service
public class RegisterSubAccountUseCase extends BaseUseCase<RegisterSubAccountCommand, RegisterSubAccountResult> {
    private static final Logger log = LoggerFactory.getLogger(RegisterSubAccountUseCase.class);


    private final SubAccountRepository subAccountRepository;
    private final AccountNameResolutionPort accountNameResolutionPort;
    private final DomainEventPublisher eventPublisher;

    public RegisterSubAccountUseCase(SubAccountRepository subAccountRepository, 
                                     AccountNameResolutionPort accountNameResolutionPort, 
                                     DomainEventPublisher eventPublisher) {
        this.subAccountRepository = subAccountRepository;
        this.accountNameResolutionPort = accountNameResolutionPort;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public RegisterSubAccountResult execute(RegisterSubAccountCommand input) {
        log.info("Executing RegisterSubAccountUseCase");

        String accountName = accountNameResolutionPort.resolve(input.bankCode(), input.accountNumber());
        
        SubAccount subAccount = new SubAccount(subAccountRepository.nextIdentity(),
                input.merchantId(),
                input.bankCode(),
                input.accountNumber(),
                accountName,
                input.description()
        );
        
        subAccountRepository.save(subAccount);
        publishEvents(subAccount, eventPublisher);
        
        return new RegisterSubAccountResult(subAccount.getId(), accountName);
    }
}



