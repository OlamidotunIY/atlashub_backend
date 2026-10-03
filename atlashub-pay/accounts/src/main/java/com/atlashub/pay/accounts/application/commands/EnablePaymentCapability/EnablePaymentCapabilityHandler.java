package com.atlashub.pay.accounts.application.commands.EnablePaymentCapability;

import com.atlashub.pay.accounts.domain.entities.OrganizationProviderProfile;
import com.atlashub.pay.accounts.domain.repositories.OrganizationProviderProfileRepository;
import com.atlashub.pay.accounts.domain.valueobject.PaymentCapability;
import com.atlashub.pay.accounts.domain.valueobject.PaymentProvider;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class EnablePaymentCapabilityHandler
        extends Command<EnablePaymentCapabilityCommand, EnablePaymentCapabilityResult> {
    private final OrganizationProviderProfileRepository repository;

    public EnablePaymentCapabilityHandler(OrganizationProviderProfileRepository repository) {
        this.repository = repository;
    }

    @Override
    public EnablePaymentCapabilityResult execute(EnablePaymentCapabilityCommand command) {
        ApiEnvironment environment = ApiEnvironment.parse(command.environment());
        PaymentCapability capability = PaymentCapability.valueOf(command.capability().toUpperCase());
        PaymentProvider provider = resolveProvider(capability, command.terminalProvider());

        OrganizationProviderProfile existing = repository
                .findByOrganizationIdAndEnvironmentAndProvider(command.organizationId(), environment, provider)
                .orElse(null);
        if (existing != null) {
            existing.requestCapabilities(initialCapabilities(capability));
            repository.save(existing);
            return new EnablePaymentCapabilityResult(existing.getId(), capability.name(), existing.getStatus().name());
        }

        OrganizationProviderProfile profile = OrganizationProviderProfile.request(
                repository.nextIdentity(), command.organizationId(), environment, provider,
                initialCapabilities(capability), null);
        profile = repository.save(profile);
        return new EnablePaymentCapabilityResult(profile.getId(), capability.name(), profile.getStatus().name());
    }

    private Set<PaymentCapability> initialCapabilities(PaymentCapability requested) {
        if (requested == PaymentCapability.CARD_COLLECTION || requested == PaymentCapability.USSD_COLLECTION) {
            return Set.of(PaymentCapability.CARD_COLLECTION, PaymentCapability.USSD_COLLECTION);
        }
        return Set.of(requested);
    }

    private PaymentProvider resolveProvider(PaymentCapability capability, String terminalProvider) {
        return switch (capability) {
            case BANKING, BANK_TRANSFER_COLLECTION -> PaymentProvider.ANCHOR;
            case CARD_COLLECTION, USSD_COLLECTION -> PaymentProvider.PAYSTACK;
            case POS_TERMINAL -> resolveTerminalProvider(terminalProvider);
        };
    }

    private PaymentProvider resolveTerminalProvider(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Terminal provider is required for POS_TERMINAL");
        }
        PaymentProvider provider = PaymentProvider.valueOf(value.toUpperCase());
        if (provider != PaymentProvider.PAYSTACK
                && provider != PaymentProvider.MONIEPOINT
                && provider != PaymentProvider.OPAY) {
            throw new IllegalArgumentException("Terminal provider must be PAYSTACK, MONIEPOINT, or OPAY");
        }
        return provider;
    }
}
