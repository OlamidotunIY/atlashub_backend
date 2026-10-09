package com.atlashub.pay.accounts.domain.entities;

import com.atlashub.pay.accounts.domain.events.PaymentProviderProfileActivatedEvent;
import com.atlashub.pay.accounts.domain.events.ProviderOnboardingRequestedEvent;
import com.atlashub.pay.accounts.domain.exceptions.InvalidProviderProfileStateException;
import com.atlashub.pay.accounts.domain.valueobject.PaymentCapability;
import com.atlashub.pay.accounts.domain.valueobject.PaymentProvider;
import com.atlashub.pay.accounts.domain.valueobject.ProviderProfileStatus;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.CorrelationId;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
public class OrganizationProviderProfile extends AggregateRoot<Long> {
    private final Long id;
    private final Long organizationId;
    private final ApiEnvironment environment;
    private final PaymentProvider provider;
    private ProviderProfileStatus status;
    private String externalMerchantId;
    private String externalAccountId;
    private String settlementAccountReference;
    private final Set<PaymentCapability> requestedCapabilities;
    private final Set<PaymentCapability> activeCapabilities;
    private Long onboardingCaseId;
    private String failureCode;
    private String failureMessage;
    private final ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public OrganizationProviderProfile(Long id, Long organizationId, ApiEnvironment environment,
                                       PaymentProvider provider, ProviderProfileStatus status,
                                       String externalMerchantId, String externalAccountId,
                                       String settlementAccountReference, Set<PaymentCapability> requestedCapabilities,
                                       Set<PaymentCapability> activeCapabilities, Long onboardingCaseId,
                                       String failureCode, String failureMessage, ZonedDateTime createdAt,
                                       ZonedDateTime updatedAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.environment = environment;
        this.provider = provider;
        this.status = status;
        this.externalMerchantId = externalMerchantId;
        this.externalAccountId = externalAccountId;
        this.settlementAccountReference = settlementAccountReference;
        this.requestedCapabilities =
                requestedCapabilities == null ? new HashSet<>() : new HashSet<>(requestedCapabilities);
        this.activeCapabilities = activeCapabilities == null ? new HashSet<>() : new HashSet<>(activeCapabilities);
        this.onboardingCaseId = onboardingCaseId;
        this.failureCode = failureCode;
        this.failureMessage = failureMessage;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static OrganizationProviderProfile request(Long id, Long organizationId, ApiEnvironment environment,
                                                      PaymentProvider provider, Set<PaymentCapability> capabilities,
                                                      Long onboardingCaseId) {
        if (id == null || organizationId == null || environment == null || provider == null || capabilities == null ||
                capabilities.isEmpty()) {
            throw new IllegalArgumentException(
                    "Provider profile identity, environment, provider, and capabilities are required");
        }
        ZonedDateTime now = ZonedDateTime.now();
        OrganizationProviderProfile profile = new OrganizationProviderProfile(id, organizationId, environment, provider,
                ProviderProfileStatus.PENDING_ONBOARDING, null, null, null, capabilities, Set.of(), onboardingCaseId,
                null, null, now, now);
        profile.registerEvent(
                new ProviderOnboardingRequestedEvent(UUID.randomUUID().toString(), id, now, CorrelationId.getOrCreate(),
                        new ProviderOnboardingRequestedEvent.Payload(organizationId, environment.name(),
                                provider.name(), capabilities.stream().map(Enum::name)
                                .collect(java.util.stream.Collectors.toUnmodifiableSet()), now)));
        return profile;
    }

    /**
     * Test collections use AtlasHub's shared Paystack sandbox merchant. They do not require
     * merchant onboarding or a banking resource for the organization.
     */
    public static OrganizationProviderProfile activateTestProfile(Long id, Long organizationId,
                                                                  String platformMerchantId) {
        if (id == null || organizationId == null || platformMerchantId == null || platformMerchantId.isBlank()) {
            throw new IllegalArgumentException("Test provider profile identity and merchant id are required");
        }
        ZonedDateTime now = ZonedDateTime.now();
        return new OrganizationProviderProfile(id, organizationId, ApiEnvironment.TEST, PaymentProvider.PAYSTACK,
                ProviderProfileStatus.ACTIVE, platformMerchantId, platformMerchantId, null,
                Set.of(PaymentCapability.CARD_COLLECTION, PaymentCapability.USSD_COLLECTION),
                Set.of(PaymentCapability.CARD_COLLECTION, PaymentCapability.USSD_COLLECTION), null,
                null, null, now, now);
    }

    public void requestCapabilities(Set<PaymentCapability> capabilities) {
        if (capabilities == null || capabilities.isEmpty()) {
            throw new InvalidProviderProfileStateException("At least one payment capability is required");
        }
        Set<PaymentCapability> additions = new HashSet<>(capabilities);
        additions.removeAll(requestedCapabilities);
        if (additions.isEmpty()) {
            return;
        }
        requestedCapabilities.addAll(additions);
        if (activeCapabilities.isEmpty()) {
            status = ProviderProfileStatus.PENDING_ONBOARDING;
        }
        touch();
        registerEvent(new ProviderOnboardingRequestedEvent(UUID.randomUUID().toString(), id, updatedAt,
                CorrelationId.getOrCreate(),
                new ProviderOnboardingRequestedEvent.Payload(organizationId, environment.name(), provider.name(),
                        additions.stream().map(Enum::name).collect(java.util.stream.Collectors.toUnmodifiableSet()),
                        updatedAt)));
    }

    public void activate(Long onboardingCaseId, String externalMerchantId, String externalAccountId,
                         String settlementAccountReference, Set<PaymentCapability> capabilities) {
        if (status == ProviderProfileStatus.REJECTED || status == ProviderProfileStatus.SUSPENDED) {
            throw new InvalidProviderProfileStateException(
                    "Rejected or suspended provider profile cannot be activated");
        }
        if (externalMerchantId == null || externalMerchantId.isBlank()) {
            throw new InvalidProviderProfileStateException("External merchant identifier is required for activation");
        }
        if (capabilities == null || capabilities.isEmpty() || !requestedCapabilities.containsAll(capabilities)) {
            throw new InvalidProviderProfileStateException("Only requested capabilities can be activated");
        }
        boolean firstActivation = activeCapabilities.isEmpty();
        this.externalMerchantId = externalMerchantId;
        this.onboardingCaseId = onboardingCaseId;
        this.externalAccountId = externalAccountId;
        this.settlementAccountReference = settlementAccountReference;
        this.activeCapabilities.addAll(capabilities);
        this.status = ProviderProfileStatus.ACTIVE;
        this.failureCode = null;
        this.failureMessage = null;
        touch();
        if (firstActivation) {
            registerEvent(new PaymentProviderProfileActivatedEvent(UUID.randomUUID().toString(), id, updatedAt,
                    CorrelationId.getOrCreate(),
                    new PaymentProviderProfileActivatedEvent.Payload(organizationId, environment.name(),
                            provider.name(), activeCapabilities.stream().map(Enum::name)
                            .collect(java.util.stream.Collectors.toUnmodifiableSet()), updatedAt)));
        }
    }

    public void requireInformation(String message) {
        this.status = ProviderProfileStatus.INFORMATION_REQUIRED;
        this.failureMessage = message;
        touch();
    }

    public void applyOnboardingStatus(String onboardingStatus, String failureCode, String failureMessage) {
        if (onboardingStatus == null || onboardingStatus.isBlank()) {
            throw new InvalidProviderProfileStateException("Provider onboarding status is required");
        }
        ProviderProfileStatus providerStatus = switch (onboardingStatus.toUpperCase()) {
            case "READY_TO_SUBMIT", "SUBMITTED", "UNDER_REVIEW" -> ProviderProfileStatus.PROVISIONING;
            case "INFORMATION_REQUIRED" -> ProviderProfileStatus.INFORMATION_REQUIRED;
            case "REJECTED" -> ProviderProfileStatus.REJECTED;
            case "SUSPENDED" -> ProviderProfileStatus.SUSPENDED;
            case "ERROR" -> ProviderProfileStatus.FAILED;
            default -> throw new InvalidProviderProfileStateException(
                    "Unsupported provider onboarding status: " + onboardingStatus);
        };
        this.status = !activeCapabilities.isEmpty() &&
                providerStatus != ProviderProfileStatus.SUSPENDED ? ProviderProfileStatus.ACTIVE : providerStatus;
        this.failureCode = failureCode;
        this.failureMessage = failureMessage;
        touch();
    }

    public boolean supports(PaymentCapability capability) {
        return status == ProviderProfileStatus.ACTIVE && activeCapabilities.contains(capability);
    }

    private void touch() {
        updatedAt = ZonedDateTime.now();
    }

    @Override
    public Long getId() {
        return id;
    }
}
