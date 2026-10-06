package com.atlashub.compliance.domain.entities;

import com.atlashub.compliance.domain.events.ProviderOnboardingApprovedEvent;
import com.atlashub.compliance.domain.events.ProviderOnboardingStatusChangedEvent;
import com.atlashub.compliance.domain.exception.InvalidProviderOnboardingStateException;
import com.atlashub.compliance.domain.valueobject.ComplianceProvider;
import com.atlashub.compliance.domain.valueobject.ProviderOnboardingStatus;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.CorrelationId;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Getter
public class ProviderOnboardingCase extends AggregateRoot<Long> {
    private final Long id;
    private final Long organizationId;
    private final ApiEnvironment environment;
    private final ComplianceProvider provider;
    private final Set<String> requestedCapabilities;
    private ProviderOnboardingStatus status;
    private String externalApplicationId;
    private Map<String, String> outstandingRequirements;
    private String failureCode;
    private String failureMessage;
    private ZonedDateTime submittedAt;
    private ZonedDateTime approvedAt;
    private final ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public ProviderOnboardingCase(
            Long id, Long organizationId, ApiEnvironment environment, ComplianceProvider provider,
            Set<String> requestedCapabilities, ProviderOnboardingStatus status,
            String externalApplicationId, Map<String, String> outstandingRequirements,
            String failureCode, String failureMessage, ZonedDateTime submittedAt,
            ZonedDateTime approvedAt, ZonedDateTime createdAt, ZonedDateTime updatedAt
    ) {
        this.id = id;
        this.organizationId = organizationId;
        this.environment = environment;
        this.provider = provider;
        this.requestedCapabilities = requestedCapabilities == null ? new HashSet<>() : new HashSet<>(requestedCapabilities);
        this.status = status;
        this.externalApplicationId = externalApplicationId;
        this.outstandingRequirements = outstandingRequirements == null ? Map.of() : Map.copyOf(outstandingRequirements);
        this.failureCode = failureCode;
        this.failureMessage = failureMessage;
        this.submittedAt = submittedAt;
        this.approvedAt = approvedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ProviderOnboardingCase request(
            Long id, Long organizationId, ApiEnvironment environment,
            ComplianceProvider provider, Set<String> capabilities
    ) {
        if (id == null || organizationId == null || environment == null || provider == null
                || capabilities == null || capabilities.isEmpty()) {
            throw new InvalidProviderOnboardingStateException("Provider onboarding identity, environment, provider, and capabilities are required");
        }
        ZonedDateTime now = ZonedDateTime.now();
        return new ProviderOnboardingCase(id, organizationId, environment, provider, capabilities,
                ProviderOnboardingStatus.READY_TO_SUBMIT, null, Map.of(), null, null,
                null, null, now, now);
    }

    public void requireInformation(Map<String, String> requirements) {
        if (requirements == null || requirements.isEmpty()) {
            throw new InvalidProviderOnboardingStateException("Outstanding provider requirements are required");
        }
        this.outstandingRequirements = Map.copyOf(requirements);
        this.status = ProviderOnboardingStatus.INFORMATION_REQUIRED;
        touch();
        publishStatusChanged();
    }

    public void requestCapabilities(Set<String> capabilities) {
        if (capabilities == null || capabilities.isEmpty()) {
            throw new InvalidProviderOnboardingStateException("At least one provider capability is required");
        }
        boolean changed = requestedCapabilities.addAll(capabilities);
        if (changed && status == ProviderOnboardingStatus.APPROVED) {
            status = ProviderOnboardingStatus.READY_TO_SUBMIT;
            approvedAt = null;
        }
        touch();
    }

    public void markSubmitted(String externalApplicationId) {
        if (status != ProviderOnboardingStatus.READY_TO_SUBMIT
                && status != ProviderOnboardingStatus.ERROR) {
            throw new InvalidProviderOnboardingStateException("Provider onboarding is not ready for submission");
        }
        if (externalApplicationId == null || externalApplicationId.isBlank()) {
            throw new InvalidProviderOnboardingStateException("External application identifier is required");
        }
        this.externalApplicationId = externalApplicationId;
        this.status = ProviderOnboardingStatus.SUBMITTED;
        this.submittedAt = ZonedDateTime.now();
        this.outstandingRequirements = Map.of();
        touch();
        publishStatusChanged();
    }

    public void markUnderReview() {
        if (status != ProviderOnboardingStatus.SUBMITTED) {
            throw new InvalidProviderOnboardingStateException("Only submitted onboarding can enter review");
        }
        status = ProviderOnboardingStatus.UNDER_REVIEW;
        touch();
        publishStatusChanged();
    }

    public void recordError(String code, String message) {
        this.failureCode = code;
        this.failureMessage = message;
        this.status = ProviderOnboardingStatus.ERROR;
        touch();
        publishStatusChanged();
    }

    public void reject(String code, String message) {
        this.failureCode = code;
        this.failureMessage = message;
        this.status = ProviderOnboardingStatus.REJECTED;
        touch();
        publishStatusChanged();
    }

    public void suspend(String code, String message) {
        this.failureCode = code;
        this.failureMessage = message;
        this.status = ProviderOnboardingStatus.SUSPENDED;
        touch();
        publishStatusChanged();
    }

    public void approve(String externalMerchantId, String externalAccountId, String settlementAccountReference) {
        if (status != ProviderOnboardingStatus.SUBMITTED
                && status != ProviderOnboardingStatus.UNDER_REVIEW) {
            throw new InvalidProviderOnboardingStateException("Only submitted provider onboarding can be approved");
        }
        this.status = ProviderOnboardingStatus.APPROVED;
        this.approvedAt = ZonedDateTime.now();
        touch();
        registerEvent(new ProviderOnboardingApprovedEvent(
                UUID.randomUUID().toString(), id, approvedAt, CorrelationId.getOrCreate(),
                new ProviderOnboardingApprovedEvent.Payload(
                        id, organizationId, environment.name(), provider.name(),
                        Set.copyOf(requestedCapabilities), externalApplicationId, externalMerchantId,
                        externalAccountId, settlementAccountReference, approvedAt)));
    }

    private void touch() { updatedAt = ZonedDateTime.now(); }

    private void publishStatusChanged() {
        registerEvent(new ProviderOnboardingStatusChangedEvent(
                UUID.randomUUID().toString(), id, updatedAt, CorrelationId.getOrCreate(),
                new ProviderOnboardingStatusChangedEvent.Payload(
                        id, organizationId, environment.name(), provider.name(), status.name(),
                        failureCode, failureMessage, updatedAt)));
    }

    @Override public Long getId() { return id; }
}
