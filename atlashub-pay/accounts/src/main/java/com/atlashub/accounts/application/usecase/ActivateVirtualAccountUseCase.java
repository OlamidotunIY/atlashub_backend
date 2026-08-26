package com.atlashub.accounts.application.usecase;

import org.springframework.stereotype.Service;

import com.atlashub.accounts.application.command.ActivateVirtualAccountCommand;
import com.atlashub.accounts.domain.model.VirtualAccount;
import com.atlashub.accounts.domain.repository.VirtualAccountDomainRepository;
import com.atlashub.shared.domain.valueobject.NUBAN;
import com.atlashub.shared.event.DomainEventPublisher;
import com.atlashub.shared.exception.NotFoundException;
import com.atlashub.accounts.domain.exception.AccountsErrorCode;
import com.atlashub.shared.usecase.BaseUseCase;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ActivateVirtualAccountUseCase extends BaseUseCase<ActivateVirtualAccountCommand, Void> {

    private final VirtualAccountDomainRepository repository;
    private final DomainEventPublisher eventPublisher;

    @Override
    @org.springframework.transaction.annotation.Transactional
    public Void execute(ActivateVirtualAccountCommand command) {
        VirtualAccount account = repository.findById(Long.valueOf(command.referenceId()))
                .orElseThrow(() -> new NotFoundException(AccountsErrorCode.ACCOUNT_NOT_FOUND, "Account not found"));
        
        account.activate(new NUBAN(command.nuban()));
        
        repository.save(account);
        publishEvents(account, eventPublisher);
        
        return null;
    }
}



