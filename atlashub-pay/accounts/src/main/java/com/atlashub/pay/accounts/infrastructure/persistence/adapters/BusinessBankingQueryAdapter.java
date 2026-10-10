package com.atlashub.pay.accounts.infrastructure.persistence.adapters;

import com.atlashub.pay.accounts.domain.repositories.BusinessDepositAccountRepository;
import com.atlashub.pay.accounts.domain.valueobject.ExternalAccountStatus;
import com.atlashub.shared.application.port.BusinessBankingQueryPort;
import com.atlashub.shared.application.security.ApiEnvironment;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BusinessBankingQueryAdapter implements BusinessBankingQueryPort {
    private final BusinessDepositAccountRepository repository;
    public BusinessBankingQueryAdapter(BusinessDepositAccountRepository repository) { this.repository = repository; }
    @Override public Optional<OperatingBankAccount> findOperatingAccount(Long organizationId, ApiEnvironment environment) {
        return repository.findByOrganizationIdAndEnvironment(organizationId, environment)
                .filter(account -> account.getStatus() == ExternalAccountStatus.ACTIVE)
                .map(account ->
                new OperatingBankAccount(account.getId(), account.getAccountName(), account.getMaskedAccountNumber(),
                        account.getBankName(), account.getBankCode(), account.getCurrency().name(),
                        account.getStatus().name(), account.getActivatedAt()));
    }
}
