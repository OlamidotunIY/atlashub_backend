package com.atlashub.pay.accounts.infrastructure.persistence.adapters;

import com.atlashub.pay.accounts.domain.repositories.OrganizationProviderProfileRepository;
import com.atlashub.pay.accounts.domain.valueobject.PaymentProvider;
import com.atlashub.pay.accounts.domain.valueobject.ProviderProfileStatus;
import com.atlashub.shared.application.port.SettlementRouteQueryPort;
import com.atlashub.shared.application.security.ApiEnvironment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SettlementRouteQueryPortAdapter implements SettlementRouteQueryPort {
    private final OrganizationProviderProfileRepository repository;
    public SettlementRouteQueryPortAdapter(OrganizationProviderProfileRepository repository) {
        this.repository = repository;
    }
    @Override
    public List<SettlementRoute> findActiveRoutes(ApiEnvironment environment) {
        return repository.findByEnvironmentAndProviderAndStatus(environment, PaymentProvider.PAYSTACK,
                        ProviderProfileStatus.ACTIVE).stream()
                .filter(profile -> profile.getExternalMerchantId() != null &&
                        profile.getSettlementAccountReference() != null)
                .map(profile -> new SettlementRoute(profile.getOrganizationId(), profile.getEnvironment(),
                        profile.getProvider().name(), profile.getExternalMerchantId(),
                        Long.valueOf(profile.getSettlementAccountReference())))
                .toList();
    }
}
