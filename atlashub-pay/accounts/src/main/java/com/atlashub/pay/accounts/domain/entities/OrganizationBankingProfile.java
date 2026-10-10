package com.atlashub.pay.accounts.domain.entities;

import com.atlashub.pay.accounts.domain.events.OrganizationBankingActivatedEvent;
import com.atlashub.pay.accounts.domain.events.OrganizationBankingProvisioningFailedEvent;
import com.atlashub.pay.accounts.domain.exceptions.InvalidBankingAccountDataException;
import com.atlashub.pay.accounts.domain.exceptions.InvalidBankingStateException;
import com.atlashub.pay.accounts.domain.valueobject.BankingProfileStatus;
import com.atlashub.pay.accounts.domain.valueobject.BankingRestrictionType;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.CorrelationId;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
public class OrganizationBankingProfile extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final ApiEnvironment environment;
    private final String anchorBusinessCustomerId;
    private Long businessDepositAccountId;
    private Long businessSubAccountId;
    private BankingProfileStatus status;
    private final Set<BankingRestrictionType> activeRestrictions;
    private String failureCode;
    private String failureMessage;
    private final ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public OrganizationBankingProfile(Long id, Long organizationId, ApiEnvironment environment,
                                      String anchorBusinessCustomerId, Long businessDepositAccountId,
                                      Long businessSubAccountId, BankingProfileStatus status,
                                      Set<BankingRestrictionType> activeRestrictions, String failureCode,
                                      String failureMessage, ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.environment = environment;
        this.anchorBusinessCustomerId = anchorBusinessCustomerId;
        this.businessDepositAccountId = businessDepositAccountId;
        this.businessSubAccountId = businessSubAccountId;
        this.status = status;
        this.activeRestrictions = activeRestrictions == null ? new HashSet<>() : new HashSet<>(activeRestrictions);
        this.failureCode = failureCode;
        this.failureMessage = failureMessage;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static OrganizationBankingProfile create(Long id, Long organizationId, ApiEnvironment environment,
                                                    String anchorBusinessCustomerId) {
        if (id == null || organizationId == null || environment == null || anchorBusinessCustomerId == null ||
                anchorBusinessCustomerId.isBlank()) {
            throw new InvalidBankingAccountDataException(
                    "Profile id, organization id and Anchor customer id are required");
        }
        ZonedDateTime now = ZonedDateTime.now();
        return new OrganizationBankingProfile(id, organizationId, environment, anchorBusinessCustomerId, null, null,
                BankingProfileStatus.PENDING, Set.of(), null, null, now, now);
    }

    public void linkDepositAccount(Long accountId) {
        this.businessDepositAccountId = accountId;
        this.status = BankingProfileStatus.PROVISIONING_DEPOSIT;
        touch();
    }

    public void linkSubAccount(Long accountId) {
        this.businessSubAccountId = accountId;
        this.status = BankingProfileStatus.PROVISIONING_SUBACCOUNT;
        touch();
    }

    public void activate() {
        if (businessDepositAccountId == null) {
            throw new InvalidBankingStateException("Deposit account is required");
        }
        boolean firstActivation = this.status != BankingProfileStatus.ACTIVE;
        this.status = activeRestrictions.isEmpty() ? BankingProfileStatus.ACTIVE : BankingProfileStatus.SUSPENDED;
        touch();
        if (firstActivation && status == BankingProfileStatus.ACTIVE) {
            registerEvent(new OrganizationBankingActivatedEvent(UUID.randomUUID().toString(), id, ZonedDateTime.now(),
                    CorrelationId.getOrCreate(),
                    new OrganizationBankingActivatedEvent.Payload(organizationId, id, businessDepositAccountId,
                            businessSubAccountId, environment.name(), "NGN", ZonedDateTime.now())));
        }
    }

    public void restrict(BankingRestrictionType restriction) {
        activeRestrictions.add(restriction);
        if (status == BankingProfileStatus.ACTIVE) status = BankingProfileStatus.SUSPENDED;
        touch();
    }

    public void removeRestriction(BankingRestrictionType restriction) {
        activeRestrictions.remove(restriction);
        touch();
    }

    public void reactivate() {
        if (!activeRestrictions.isEmpty())
            throw new InvalidBankingStateException("Restricted banking profile cannot be reactivated");
        if (status == BankingProfileStatus.SUSPENDED) status = BankingProfileStatus.ACTIVE;
        touch();
    }

    public void markPartiallyProvisioned(String code, String message) {
        status = BankingProfileStatus.PARTIALLY_PROVISIONED;
        failureCode = code;
        failureMessage = message;
        touch();
    }

    public void fail(String code, String message) {
        status = BankingProfileStatus.FAILED;
        failureCode = code;
        failureMessage = message;
        touch();
        registerEvent(
                new OrganizationBankingProvisioningFailedEvent(UUID.randomUUID().toString(), id, ZonedDateTime.now(),
                        CorrelationId.getOrCreate(),
                        new OrganizationBankingProvisioningFailedEvent.Payload(organizationId, environment.name(), code,
                                message, ZonedDateTime.now())));
    }

    public boolean isUsable() {
        return status == BankingProfileStatus.ACTIVE && activeRestrictions.isEmpty();
    }

    private void touch() {
        updatedAt = ZonedDateTime.now();
    }

    @Override
    public Long getId() {
        return id;
    }
}
