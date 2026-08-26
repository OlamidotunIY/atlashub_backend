package com.atlashub.accounts.application.usecase;

import org.springframework.stereotype.Service;

import com.atlashub.accounts.application.command.ForceCloseAccountsCommand;
import com.atlashub.accounts.domain.model.VirtualAccount;
import com.atlashub.accounts.domain.repository.VirtualAccountDomainRepository;
import com.atlashub.shared.event.DomainEventPublisher;
import com.atlashub.shared.usecase.BaseUseCase;
import lombok.RequiredArgsConstructor;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ForceCloseAccountsUseCase extends BaseUseCase<ForceCloseAccountsCommand, Void> {

    private final VirtualAccountDomainRepository repository;
    private final DomainEventPublisher eventPublisher;

    @Override
    @org.springframework.transaction.annotation.Transactional
    public Void execute(ForceCloseAccountsCommand command) {
        List<VirtualAccount> accounts = repository.findByIntegration(command.integration());
        
        for (VirtualAccount account : accounts) {
            account.close();
            repository.save(account);
            publishEvents(account, eventPublisher);
        }
        
        return null;
    }
}



