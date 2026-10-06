package com.atlashub.pay.accounts.infrastructure.persistence.adapters;

import com.atlashub.pay.accounts.domain.repositories.OrganizationProviderProfileRepository;
import com.atlashub.pay.accounts.domain.valueobject.PaymentCapability;
import com.atlashub.shared.application.port.PaymentProviderProfileQueryPort;
import com.atlashub.shared.application.security.ApiEnvironment;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PaymentProviderProfileQueryPortAdapter implements PaymentProviderProfileQueryPort {
    private final OrganizationProviderProfileRepository repository;

    public PaymentProviderProfileQueryPortAdapter(OrganizationProviderProfileRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<ProviderProfile> findActiveProfile(
            Long organizationId, ApiEnvironment environment, String capability) {
        PaymentCapability required = PaymentCapability.valueOf(capability);
        return repository.findByOrganizationIdAndEnvironment(organizationId, environment).stream()
                .filter(profile -> profile.supports(required))
                .findFirst()
                .map(profile -> new ProviderProfile(
                        profile.getId(), profile.getOrganizationId(), profile.getEnvironment(),
                        profile.getProvider().name(), profile.getExternalMerchantId(),
                        profile.getSettlementAccountReference(), profile.getActiveCapabilities().stream()
                        .map(Enum::name).collect(java.util.stream.Collectors.toUnmodifiableSet())));
    }
}
