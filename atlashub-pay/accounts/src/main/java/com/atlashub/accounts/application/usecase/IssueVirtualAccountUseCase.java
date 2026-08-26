package com.atlashub.accounts.application.usecase;

import org.springframework.stereotype.Service;

import com.atlashub.accounts.application.command.IssueVirtualAccountCommand;
import com.atlashub.accounts.domain.model.VirtualAccount;
import com.atlashub.accounts.domain.repository.VirtualAccountDomainRepository;
import com.atlashub.shared.event.DomainEventPublisher;
import com.atlashub.shared.usecase.BaseUseCase;
import lombok.RequiredArgsConstructor;

import com.atlashub.accounts.application.port.VirtualAccountQueryService;
import com.atlashub.shared.exception.BusinessRuleException;
import com.atlashub.accounts.domain.exception.AccountsErrorCode;

@RequiredArgsConstructor
@Service
public class IssueVirtualAccountUseCase extends BaseUseCase<IssueVirtualAccountCommand, Long> {

    private final VirtualAccountDomainRepository repository;
    private final VirtualAccountQueryService queryService;
    private final DomainEventPublisher eventPublisher;

    @Override
    @org.springframework.transaction.annotation.Transactional
    public Long execute(IssueVirtualAccountCommand command) {
        
        if (queryService.countByIntegration(command.integration()) >= 2) {
            throw new BusinessRuleException(AccountsErrorCode.INVALID_ACCOUNT_STATE, "Merchant can have at most 2 accounts");
        }
        if (queryService.existsByIntegrationAndBankName(command.integration(), command.bankName())) {
            throw new BusinessRuleException(AccountsErrorCode.INVALID_ACCOUNT_STATE, "Merchant already has an account with " + command.bankName());
        }

        VirtualAccount account = VirtualAccount.create(repository.nextIdentity(), 
                command.integration(), 
                command.customerCode(), 
                command.accountName(), 
                command.bankName(),
                command.idempotencyKey(),
                command.currency()
        );
        
        VirtualAccount savedAccount = repository.save(account);
        publishEvents(savedAccount, eventPublisher);
        
        return savedAccount.getId();
    }
}



