package com.atlashub.accounts.application.usecase;

import org.springframework.stereotype.Service;

import com.atlashub.accounts.application.command.RequestClosureCommand;
import com.atlashub.accounts.domain.model.VirtualAccount;
import com.atlashub.accounts.domain.repository.VirtualAccountDomainRepository;
import com.atlashub.shared.event.DomainEventPublisher;
import com.atlashub.shared.exception.NotFoundException;
import com.atlashub.accounts.domain.exception.AccountsErrorCode;
import com.atlashub.shared.usecase.BaseUseCase;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class RequestClosureUseCase extends BaseUseCase<RequestClosureCommand, Void> {

    private final VirtualAccountDomainRepository repository;
    private final DomainEventPublisher eventPublisher;

    @Override
    @org.springframework.transaction.annotation.Transactional
    public Void execute(RequestClosureCommand command) {
        VirtualAccount account = repository.findById(Long.valueOf(command.accountId()))
                .orElseThrow(() -> new NotFoundException(AccountsErrorCode.ACCOUNT_NOT_FOUND, "Account not found"));
        
        account.requestClosure();
        
        repository.save(account);
        publishEvents(account, eventPublisher);
        
        return null;
    }
}



