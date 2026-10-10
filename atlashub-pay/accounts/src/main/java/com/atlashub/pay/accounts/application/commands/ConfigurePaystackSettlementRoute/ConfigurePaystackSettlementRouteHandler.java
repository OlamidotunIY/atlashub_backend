package com.atlashub.pay.accounts.application.commands.ConfigurePaystackSettlementRoute;

import com.atlashub.pay.accounts.domain.entities.BusinessDepositAccount;
import com.atlashub.pay.accounts.domain.entities.OrganizationProviderProfile;
import com.atlashub.shared.application.port.PaystackSettlementOnboardingPort;
import com.atlashub.pay.accounts.domain.repositories.BusinessDepositAccountRepository;
import com.atlashub.pay.accounts.domain.repositories.OrganizationProviderProfileRepository;
import com.atlashub.pay.accounts.domain.valueobject.ExternalAccountStatus;
import com.atlashub.pay.accounts.domain.valueobject.PaymentCapability;
import com.atlashub.pay.accounts.domain.valueobject.PaymentProvider;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ConfigurePaystackSettlementRouteHandler
        extends Command<ConfigurePaystackSettlementRouteCommand, Void> {
    private static final Set<PaymentCapability> COLLECTIONS =
            Set.of(PaymentCapability.CARD_COLLECTION, PaymentCapability.USSD_COLLECTION);
    private final BusinessDepositAccountRepository depositRepository;
    private final OrganizationProviderProfileRepository profileRepository;
    private final PaystackSettlementOnboardingPort onboardingPort;

    public ConfigurePaystackSettlementRouteHandler(BusinessDepositAccountRepository depositRepository,
            OrganizationProviderProfileRepository profileRepository, PaystackSettlementOnboardingPort onboardingPort) {
        this.depositRepository = depositRepository;
        this.profileRepository = profileRepository;
        this.onboardingPort = onboardingPort;
    }

    @Override public Void execute(ConfigurePaystackSettlementRouteCommand command) {
        if (command.environment() != ApiEnvironment.LIVE) return null;
        BusinessDepositAccount account = depositRepository
                .findByOrganizationIdAndEnvironment(command.organizationId(), command.environment())
                .orElseThrow(() -> new IllegalArgumentException("Active business deposit account not found"));
        if (account.getStatus() != ExternalAccountStatus.ACTIVE || account.getAccountNumber() == null ||
                account.getBankCode() == null || account.getAccountName() == null) {
            throw new IllegalStateException("Confirmed Anchor operating-account details are required");
        }
        OrganizationProviderProfile profile = profileRepository
                .findByOrganizationIdAndEnvironmentAndProvider(command.organizationId(), command.environment(),
                        PaymentProvider.PAYSTACK)
                .orElseGet(() -> profileRepository.save(OrganizationProviderProfile.request(
                        profileRepository.nextIdentity(), command.organizationId(), command.environment(),
                        PaymentProvider.PAYSTACK, COLLECTIONS, null)));
        profile.requestCapabilities(COLLECTIONS);
        try {
            var result = onboardingPort.configure(new PaystackSettlementOnboardingPort.Request(
                    command.organizationId(), command.environment(), account.getAccountNumber(), account.getBankCode(),
                    account.getAccountName(), profile.getExternalMerchantId(),
                    "paystack-route-" + command.organizationId() + "-" + account.getId()));
            if (!result.active()) throw new IllegalStateException("Paystack settlement route is not active");
            profile.recordSettlementRoute(result.subaccountCode(), result.settlementBankCode(),
                    account.getId().toString(), COLLECTIONS);
        } catch (RuntimeException error) {
            profile.applyOnboardingStatus("ERROR", "PAYSTACK_SETTLEMENT_ROUTE_FAILED", error.getMessage());
            profileRepository.save(profile);
            throw error;
        }
        profileRepository.save(profile);
        return null;
    }
}
