package com.atlashub.accounts.application.usecase;

import com.atlashub.accounts.application.command.BootstrapOrganizationAccountsCommand;
import com.atlashub.accounts.domain.model.InternalAccount;
import com.atlashub.accounts.domain.repository.InternalAccountDomainRepository;
import com.atlashub.accounts.domain.valueobject.InternalAccountType;
import com.atlashub.shared.application.usecase.BaseUseCase;
import com.atlashub.shared.application.port.out.DomainEventPublisher;
import com.atlashub.shared.domain.money.CurrencyCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BootstrapOrganizationAccountsUseCase extends BaseUseCase<BootstrapOrganizationAccountsCommand, Void> {

    private final InternalAccountDomainRepository repository;
    private final DomainEventPublisher publisher;

    public BootstrapOrganizationAccountsUseCase(InternalAccountDomainRepository repository, DomainEventPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    @Override
    @Transactional
    public Void execute(BootstrapOrganizationAccountsCommand command) {
        Long orgId = command.organizationId();
        CurrencyCode baseCurrency = command.currency();

        bootstrapAccountIfMissing(orgId, InternalAccountType.OPERATING, baseCurrency);
        bootstrapAccountIfMissing(orgId, InternalAccountType.PAYROLL_RESERVE, baseCurrency);
        bootstrapAccountIfMissing(orgId, InternalAccountType.TAX_HOLDING, baseCurrency);
        bootstrapAccountIfMissing(orgId, InternalAccountType.ESCROW, baseCurrency);
        bootstrapAccountIfMissing(orgId, InternalAccountType.SUSPENSE, baseCurrency);

        return null;
    }

    private void bootstrapAccountIfMissing(Long orgId, InternalAccountType type, CurrencyCode currency) {
        if (!repository.existsByOrganizationIdAndType(orgId, type)) {
            InternalAccount account = InternalAccount.create(repository.nextIdentity(), orgId, type, currency);
            InternalAccount saved = repository.save(account);
            publishEvents(saved, publisher);
        }
    }
}