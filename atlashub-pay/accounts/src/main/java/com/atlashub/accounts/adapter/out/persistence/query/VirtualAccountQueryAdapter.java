package com.atlashub.accounts.adapter.out.persistence.query;

import com.atlashub.accounts.application.port.VirtualAccountQueryService;
import com.atlashub.accounts.adapter.out.persistence.repository.JpaVirtualAccountRepository;
import com.atlashub.shared.domain.valueobject.NUBAN;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VirtualAccountQueryAdapter implements VirtualAccountQueryService {

    private final JpaVirtualAccountRepository repository;

    @Override
    public int countByIntegration(Long integration) {
        return repository.findByIntegration(integration).size();
    }

    @Override
    public boolean existsByIntegrationAndBankName(Long integration, String bankName) {
        return repository.findByIntegration(integration).stream()
                .anyMatch(account -> account.getBankName().equals(bankName));
    }

    @Override
    public Optional<String> findIntegrationByNuban(NUBAN nuban) {
        return repository.findByNuban(nuban.value())
                .map(entity -> String.valueOf(entity.getIntegration()));
    }
}
