package com.atlashub.pay.accounts.domain.entities;

import com.atlashub.pay.accounts.domain.valueobject.BankingRestrictionType;
import com.atlashub.pay.accounts.domain.valueobject.ConfirmedBankingDetails;
import com.atlashub.pay.accounts.domain.valueobject.ExternalAccountStatus;
import com.atlashub.pay.accounts.domain.valueobject.ReservedAccountOwnerType;
import com.atlashub.pay.accounts.domain.exceptions.InvalidBankingAccountDataException;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.pay.accounts.domain.events.ReservedAccountActivatedEvent;
import com.atlashub.pay.accounts.domain.events.ReservedAccountClosedEvent;
import com.atlashub.pay.accounts.domain.events.ReservedAccountFundedEvent;
import com.atlashub.pay.accounts.domain.events.ReservedAccountProvisioningFailedEvent;
import com.atlashub.pay.accounts.domain.events.ReservedAccountReactivatedEvent;
import com.atlashub.pay.accounts.domain.events.ReservedAccountRequestedEvent;
import com.atlashub.pay.accounts.domain.events.ReservedAccountSuspendedEvent;
import com.atlashub.shared.domain.valueobject.CorrelationId;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import lombok.Getter;
import com.atlashub.shared.application.security.ApiEnvironment;

import java.math.BigDecimal;
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
            throw new InvalidBankingAccountDataException("Reserved account owner is required");
        if (anchorPayoutSubAccountId == null || anchorPayoutSubAccountId.isBlank())
            throw new InvalidBankingAccountDataException("Organization Anchor payout subaccount is required");
        ZonedDateTime now=ZonedDateTime.now();
        ReservedAccount account = new ReservedAccount(id,orgId,environment,ownerType,ownerReferenceId,subAccountId,anchorPayoutSubAccountId,
                provider,requestReference,null,null,null,null,null,null,null,currency,
                ExternalAccountStatus.REQUESTED,Set.of(),null,now,null,now);
        account.registerEvent(new ReservedAccountRequestedEvent(
                UUID.randomUUID().toString(), id, now, CorrelationId.getOrCreate(),
                new ReservedAccountRequestedEvent.Payload(orgId, environment.name(), ownerType.name(),
                        ownerReferenceId, requestReference, now)));
        return account;
    }

    public void markSubmitted(String reservedAccountId, String customerId) { anchorReservedAccountId=reservedAccountId; anchorCustomerId=customerId; status=ExternalAccountStatus.PENDING; touch(); }
    public void activate(ConfirmedBankingDetails d) {
        if (status == ExternalAccountStatus.ACTIVE && activatedAt != null) return;
        if (d == null || !d.hasAccountNumberDetails())
            throw new InvalidBankingAccountDataException("Confirmed reserved-account name and number are required");
        boolean firstActivation = activatedAt == null;
        accountName=d.accountName(); accountNumber=d.accountNumber(); maskedAccountNumber=d.maskedAccountNumber();
        bankName=d.bankName(); bankCode=d.bankCode();
        status=activeRestrictions.isEmpty() ? ExternalAccountStatus.ACTIVE : ExternalAccountStatus.SUSPENDED;
        if (firstActivation) activatedAt=ZonedDateTime.now();
        touch();
        if (firstActivation) {
            registerEvent(new ReservedAccountActivatedEvent(
                    UUID.randomUUID().toString(), id, ZonedDateTime.now(), CorrelationId.getOrCreate(),
                    new ReservedAccountActivatedEvent.Payload(id, organizationId, ownerType.name(), ownerReferenceId,
                            businessSubAccountId, anchorReservedAccountId, accountName, maskedAccountNumber,
                            bankName, environment.name(), currency.name(), activatedAt)));
        }
    }
    public void restrict(BankingRestrictionType reason) {
        boolean newlyRestricted = activeRestrictions.add(reason);
        if(status == ExternalAccountStatus.ACTIVE) status=ExternalAccountStatus.SUSPENDED;
        touch();
        if (newlyRestricted) registerEvent(new ReservedAccountSuspendedEvent(
                UUID.randomUUID().toString(), id, ZonedDateTime.now(), CorrelationId.getOrCreate(),
                new ReservedAccountSuspendedEvent.Payload(
                        organizationId, environment.name(), reason.name(), ZonedDateTime.now())));
    }
    public void removeRestriction(BankingRestrictionType reason) {
        boolean removed = activeRestrictions.remove(reason);
        boolean reactivated = removed && activeRestrictions.isEmpty() && status == ExternalAccountStatus.SUSPENDED;
        if(reactivated) status=ExternalAccountStatus.ACTIVE;
        touch();
        if (reactivated) registerEvent(new ReservedAccountReactivatedEvent(
                UUID.randomUUID().toString(), id, ZonedDateTime.now(), CorrelationId.getOrCreate(),
                new ReservedAccountReactivatedEvent.Payload(organizationId, environment.name(), ZonedDateTime.now())));
    }
    public void fail(String reason) {
        if (status == ExternalAccountStatus.FAILED) return;
        status=ExternalAccountStatus.FAILED; failureReason=reason; touch();
        registerEvent(new ReservedAccountProvisioningFailedEvent(
                UUID.randomUUID().toString(), id, ZonedDateTime.now(), CorrelationId.getOrCreate(),
                new ReservedAccountProvisioningFailedEvent.Payload(
                        organizationId, environment.name(), reason, ZonedDateTime.now())));
    }
    public void close() {
        if (status == ExternalAccountStatus.CLOSED) return;
        status=ExternalAccountStatus.CLOSED; activeRestrictions.clear(); touch();
        registerEvent(new ReservedAccountClosedEvent(
                UUID.randomUUID().toString(), id, ZonedDateTime.now(), CorrelationId.getOrCreate(),
                new ReservedAccountClosedEvent.Payload(organizationId, environment.name(), ZonedDateTime.now())));
    }
    public void recordFunding(String transferReference, BigDecimal amount, CurrencyCode fundingCurrency,
                              String senderAccountName, String senderBankCode, ZonedDateTime receivedAt) {
        if (transferReference == null || transferReference.isBlank() || amount == null
                || amount.signum() <= 0 || fundingCurrency == null) {
            throw new InvalidBankingAccountDataException("Valid funding reference, amount and currency are required");
        }
        registerEvent(new ReservedAccountFundedEvent(
                UUID.randomUUID().toString(), id, ZonedDateTime.now(), CorrelationId.getOrCreate(),
                new ReservedAccountFundedEvent.Payload(id, organizationId, environment.name(), ownerType.name(),
                        ownerReferenceId, businessSubAccountId, transferReference, amount, fundingCurrency.name(),
                        senderAccountName, senderBankCode, receivedAt == null ? ZonedDateTime.now() : receivedAt)));
    }
    private void touch() { updatedAt=ZonedDateTime.now(); }
    @Override public Long getId() { return id; }
}
