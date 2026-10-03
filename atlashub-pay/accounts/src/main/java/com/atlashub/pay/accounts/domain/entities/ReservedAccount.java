package com.atlashub.pay.accounts.domain.entities;

import com.atlashub.pay.accounts.domain.valueobject.BankingRestrictionType;
import com.atlashub.pay.accounts.domain.valueobject.ConfirmedBankingDetails;
import com.atlashub.pay.accounts.domain.valueobject.ExternalAccountStatus;
import com.atlashub.pay.accounts.domain.valueobject.ReservedAccountOwnerType;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.pay.accounts.domain.events.ReservedAccountActivatedEvent;
import com.atlashub.shared.domain.valueobject.CorrelationId;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import lombok.Getter;
import com.atlashub.shared.application.security.ApiEnvironment;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
public class ReservedAccount extends AggregateRoot<Long> {
    private final Long id;
    private final Long organizationId;
    private final ApiEnvironment environment;
    private final ReservedAccountOwnerType ownerType;
    private final String ownerReferenceId;
    private final Long businessSubAccountId;
    private final String anchorPayoutSubAccountId;
    private final String provider;
    private final String requestReference;
    private String anchorReservedAccountId;
    private String anchorCustomerId;
    private String accountName;
    private String accountNumber;
    private String maskedAccountNumber;
    private String bankName;
    private String bankCode;
    private final CurrencyCode currency;
    private ExternalAccountStatus status;
    private final Set<BankingRestrictionType> activeRestrictions;
    private String failureReason;
    private final ZonedDateTime createdAt;
    private ZonedDateTime activatedAt;
    private ZonedDateTime updatedAt;

    public ReservedAccount(Long id, Long organizationId, ApiEnvironment environment, ReservedAccountOwnerType ownerType,
                           String ownerReferenceId, Long businessSubAccountId, String anchorPayoutSubAccountId,
                           String provider, String requestReference, String anchorReservedAccountId,
                           String anchorCustomerId, String accountName, String accountNumber,
                           String maskedAccountNumber, String bankName, String bankCode, CurrencyCode currency,
                           ExternalAccountStatus status, Set<BankingRestrictionType> activeRestrictions,
                           String failureReason, ZonedDateTime createdAt, ZonedDateTime activatedAt,
                           ZonedDateTime updatedAt) {
        this.id=id; this.organizationId=organizationId; this.environment=environment; this.ownerType=ownerType; this.ownerReferenceId=ownerReferenceId;
        this.businessSubAccountId=businessSubAccountId; this.anchorPayoutSubAccountId=anchorPayoutSubAccountId;
        this.provider=provider; this.requestReference=requestReference; this.anchorReservedAccountId=anchorReservedAccountId;
        this.anchorCustomerId=anchorCustomerId; this.accountName=accountName; this.accountNumber=accountNumber;
        this.maskedAccountNumber=maskedAccountNumber; this.bankName=bankName; this.bankCode=bankCode;
        this.currency=currency; this.status=status;
        this.activeRestrictions=activeRestrictions == null ? new HashSet<>() : new HashSet<>(activeRestrictions);
        this.failureReason=failureReason; this.createdAt=createdAt; this.activatedAt=activatedAt; this.updatedAt=updatedAt;
    }

    public static ReservedAccount request(Long id, Long orgId, ApiEnvironment environment, ReservedAccountOwnerType ownerType,
                                          String ownerReferenceId, Long subAccountId,
                                          String anchorPayoutSubAccountId, String provider,
                                          String requestReference, CurrencyCode currency) {
        if (ownerType == null || ownerReferenceId == null || ownerReferenceId.isBlank())
            throw new IllegalArgumentException("Reserved account owner is required");
        if (anchorPayoutSubAccountId == null || anchorPayoutSubAccountId.isBlank())
            throw new IllegalArgumentException("Organization Anchor payout subaccount is required");
        ZonedDateTime now=ZonedDateTime.now();
        return new ReservedAccount(id,orgId,environment,ownerType,ownerReferenceId,subAccountId,anchorPayoutSubAccountId,
                provider,requestReference,null,null,null,null,null,null,null,currency,
                ExternalAccountStatus.REQUESTED,Set.of(),null,now,null,now);
    }

    public void markSubmitted(String reservedAccountId, String customerId) { anchorReservedAccountId=reservedAccountId; anchorCustomerId=customerId; status=ExternalAccountStatus.PENDING; touch(); }
    public void activate(ConfirmedBankingDetails d) {
        if (status == ExternalAccountStatus.ACTIVE) return;
        if (d == null) throw new IllegalArgumentException("Confirmed banking details are required");
        accountName=d.accountName(); accountNumber=d.accountNumber(); maskedAccountNumber=d.maskedAccountNumber();
        bankName=d.bankName(); bankCode=d.bankCode(); status=ExternalAccountStatus.ACTIVE;
        activatedAt=ZonedDateTime.now(); touch();
        registerEvent(new ReservedAccountActivatedEvent(
                UUID.randomUUID().toString(), id, ZonedDateTime.now(), CorrelationId.getOrCreate(),
                new ReservedAccountActivatedEvent.Payload(id, organizationId, ownerType.name(), ownerReferenceId,
                        businessSubAccountId, anchorReservedAccountId, accountName, maskedAccountNumber,
                        bankName, environment.name(), currency.name(), activatedAt)));
    }
    public void restrict(BankingRestrictionType reason) { activeRestrictions.add(reason); status=ExternalAccountStatus.SUSPENDED; touch(); }
    public void removeRestriction(BankingRestrictionType reason) { activeRestrictions.remove(reason); if(activeRestrictions.isEmpty()) status=ExternalAccountStatus.ACTIVE; touch(); }
    public void fail(String reason) { status=ExternalAccountStatus.FAILED; failureReason=reason; touch(); }
    public void close() { status=ExternalAccountStatus.CLOSED; activeRestrictions.clear(); touch(); }
    private void touch() { updatedAt=ZonedDateTime.now(); }
    @Override public Long getId() { return id; }
}
