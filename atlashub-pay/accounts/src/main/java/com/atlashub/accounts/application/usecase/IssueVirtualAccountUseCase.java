package com.atlashub.accounts.application.usecase;

import org.springframework.stereotype.Service;

import com.atlashub.accounts.application.command.IssueVirtualAccountCommand;
import com.atlashub.accounts.domain.model.VirtualAccount;
import com.atlashub.accounts.domain.repository.VirtualAccountDomainRepository;
import com.atlashub.shared.application.port.out.DomainEventPublisher;
import com.atlashub.shared.application.usecase.BaseUseCase;
import lombok.RequiredArgsConstructor;

import com.atlashub.accounts.application.port.AccountQueryService;
import com.atlashub.shared.domain.exception.BusinessRuleException;
import com.atlashub.accounts.domain.exception.AccountsErrorCode;

@RequiredArgsConstructor
@Service
public class IssueVirtualAccountUseCase extends BaseUseCase<IssueVirtualAccountCommand, Long> {

    private final VirtualAccountDomainRepository repository;
    private final AccountQueryService queryService;
    private final DomainEventPublisher eventPublisher;

    @Override
    @org.springframework.transaction.annotation.Transactional
    public Long execute(IssueVirtualAccountCommand command) {
        
        if (queryService.countVirtualAccountsByIntegration(command.integration()) >= 2) {
            throw new BusinessRuleException(AccountsErrorCode.INVALID_ACCOUNT_STATE, "Organization can have at most 2 accounts");
        }
        if (queryService.existsVirtualAccountByIntegrationAndBankName(command.integration(), command.bankName())) {
            throw new BusinessRuleException(AccountsErrorCode.INVALID_ACCOUNT_STATE, "Organization already has an account with " + command.bankName());
        }

        VirtualAccount account = VirtualAccount.create(repository.nextIdentity(), 
                command.integration(), 
                command.UserCode(), 
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
