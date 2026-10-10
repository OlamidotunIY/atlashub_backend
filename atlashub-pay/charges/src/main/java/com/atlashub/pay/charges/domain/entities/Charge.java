package com.atlashub.pay.charges.domain.entities;

import com.atlashub.pay.charges.domain.events.ChargeFailedEvent;
import com.atlashub.pay.charges.domain.events.ChargeRefundInitiatedEvent;
import com.atlashub.pay.charges.domain.events.ChargeRefundedEvent;
import com.atlashub.pay.charges.domain.events.ChargeSuccessfulEvent;
import com.atlashub.pay.charges.domain.events.ChargeDisputedEvent;
import com.atlashub.pay.charges.domain.events.ChargeDisputeResolvedEvent;
import com.atlashub.pay.charges.domain.events.ChargeInitializedEvent;
import com.atlashub.pay.charges.domain.exceptions.InvalidChargeException;
import com.atlashub.pay.charges.domain.valueobject.ChargeChannel;
import com.atlashub.pay.charges.domain.valueobject.ChargeStatus;
import com.atlashub.pay.charges.domain.valueobject.PaymentProvider;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.CorrelationId;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
public class Charge extends AggregateRoot<Long> {
    private final Long id;
    private final Long organizationId;
    private final ApiEnvironment environment;
    private final String reference;
    private final Money amount;
    private final ChargeChannel channel;
    private final PaymentProvider provider;
    private final Long providerProfileId;
    private String providerReference;
    private final String sourceSystem;
    private final String sourceReferenceId;
    private final String customerReferenceId;
    private Money providerFee;
    private ChargeStatus status;
    private String authorizationUrl;
    private String accessCode;
    private String failureMessage;
    private String providerRefundReference;
    private String refundReason;
    private ZonedDateTime refundedAt;
    private String disputeReference;
    private String disputeStatus;
    private String disputeReason;
    private ZonedDateTime successfulAt;
    private final ZonedDateTime expiresAt;
    private final ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public Charge(Long id, Long organizationId, ApiEnvironment environment, String reference, Money amount,
                  ChargeChannel channel, PaymentProvider provider, Long providerProfileId, String providerReference,
                  String sourceSystem, String sourceReferenceId, ChargeStatus status, String authorizationUrl,
                  String accessCode, String failureMessage, ZonedDateTime successfulAt, ZonedDateTime expiresAt,
                  ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this(id, organizationId, environment, reference, amount, channel, provider, providerProfileId,
                providerReference, sourceSystem, sourceReferenceId, status, authorizationUrl, accessCode,
                failureMessage, null, null, null, null, null, null, null, null,
                successfulAt, expiresAt, createdAt, updatedAt);
    }

    public Charge(Long id, Long organizationId, ApiEnvironment environment, String reference, Money amount,
                  ChargeChannel channel, PaymentProvider provider, Long providerProfileId, String providerReference,
                  String sourceSystem, String sourceReferenceId, ChargeStatus status, String authorizationUrl,
                  String accessCode, String failureMessage, String providerRefundReference, String refundReason,
                  ZonedDateTime refundedAt, String customerReferenceId, Money providerFee,
                  String disputeReference, String disputeStatus, String disputeReason,
                  ZonedDateTime successfulAt, ZonedDateTime expiresAt,
                  ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.environment = environment;
        this.reference = reference;
        this.amount = amount;
        this.channel = channel;
        this.provider = provider;
        this.providerProfileId = providerProfileId;
        this.providerReference = providerReference;
        this.sourceSystem = sourceSystem;
        this.sourceReferenceId = sourceReferenceId;
        this.customerReferenceId = customerReferenceId;
        this.providerFee = providerFee;
        this.status = status;
        this.authorizationUrl = authorizationUrl;
        this.accessCode = accessCode;
        this.failureMessage = failureMessage;
        this.providerRefundReference = providerRefundReference;
        this.refundReason = refundReason;
        this.refundedAt = refundedAt;
        this.disputeReference = disputeReference;
        this.disputeStatus = disputeStatus;
        this.disputeReason = disputeReason;
        this.successfulAt = successfulAt;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        validate();
    }

    public static Charge initialize(Long id, Long org, ApiEnvironment env, String reference, Money amount,
                                    ChargeChannel channel, Long profileId, String source, String sourceRef) {
        return initialize(id, org, env, reference, amount, channel, profileId, source, sourceRef, null);
    }

    public static Charge initialize(Long id, Long org, ApiEnvironment env, String reference, Money amount,
                                    ChargeChannel channel, Long profileId, String source, String sourceRef,
                                    String customerReferenceId) {
        ZonedDateTime now = ZonedDateTime.now();
        return new Charge(id, org, env, reference, amount, channel, PaymentProvider.PAYSTACK, profileId, null, source,
                sourceRef, ChargeStatus.INITIALIZED, null, null, null, null, null, null, customerReferenceId, null,
                null, null, null, null, now.plusMinutes(30), now, now);
    }

    public void markPending(String providerReference, String authorizationUrl, String accessCode) {
        if (status != ChargeStatus.INITIALIZED)
            throw new InvalidChargeException("Only an initialized charge can be submitted");
        if (blank(providerReference) || blank(authorizationUrl) || blank(accessCode))
            throw new InvalidChargeException("Provider checkout details are required");
        this.providerReference = providerReference;
        this.authorizationUrl = authorizationUrl;
        this.accessCode = accessCode;
        status = ChargeStatus.PENDING;
        touch();
        registerEvent(new ChargeInitializedEvent(UUID.randomUUID().toString(), id, updatedAt,
                CorrelationId.getOrCreate(), new ChargeInitializedEvent.Payload(organizationId, environment.name(),
                reference, amount, channel.name(), provider.name(), sourceSystem, sourceReferenceId,
                customerReferenceId, updatedAt)));
    }

    public void succeed(String gatewayReference, Money confirmedAmount, CurrencyCode confirmedCurrency) {
        succeed(gatewayReference, confirmedAmount, confirmedCurrency, Money.zero(confirmedCurrency));
    }

    public void succeed(String gatewayReference, Money confirmedAmount, CurrencyCode confirmedCurrency,
                        Money confirmedProviderFee) {
        if (status == ChargeStatus.SUCCESSFUL) return;
        if (status != ChargeStatus.PENDING) throw new InvalidChargeException("Only a pending charge can succeed");
        if (amount.compareTo(confirmedAmount) != 0 || amount.currency() != confirmedCurrency)
            throw new InvalidChargeException("Confirmed charge amount or currency does not match");
        if (confirmedProviderFee == null || confirmedProviderFee.currency() != confirmedCurrency
                || confirmedProviderFee.amount().signum() < 0) {
            throw new InvalidChargeException("Confirmed provider fee is invalid");
        }
        providerReference = gatewayReference;
        providerFee = confirmedProviderFee;
        status = ChargeStatus.SUCCESSFUL;
        successfulAt = ZonedDateTime.now();
        touch();
        registerEvent(
                new ChargeSuccessfulEvent(UUID.randomUUID().toString(), id, updatedAt, CorrelationId.getOrCreate(),
                        new ChargeSuccessfulEvent.Payload(organizationId, environment.name(), reference,
                        gatewayReference, amount, providerFee, channel.name(), provider.name(), sourceSystem,
                        sourceReferenceId, customerReferenceId, successfulAt)));
    }

    public void fail(String reason) {
        if (status == ChargeStatus.FAILED) return;
        if (status == ChargeStatus.SUCCESSFUL || status == ChargeStatus.REFUNDED)
            throw new InvalidChargeException("A final successful charge cannot fail");
        status = ChargeStatus.FAILED;
        failureMessage = reason;
        touch();
        registerEvent(new ChargeFailedEvent(UUID.randomUUID().toString(), id, updatedAt, CorrelationId.getOrCreate(),
                new ChargeFailedEvent.Payload(organizationId, environment.name(), reference, amount, channel.name(),
                        provider.name(), sourceSystem, sourceReferenceId, customerReferenceId, reason, updatedAt)));
    }

    public void initiateRefund(String reason) {
        if (status == ChargeStatus.REFUND_PENDING) return;
        if (status != ChargeStatus.SUCCESSFUL) {
            throw new InvalidChargeException("Only a successful charge can be refunded");
        }
        if (blank(reason)) {
            throw new InvalidChargeException("Refund reason is required");
        }
        status = ChargeStatus.REFUND_PENDING;
        refundReason = reason.trim();
        failureMessage = null;
        touch();
        registerEvent(new ChargeRefundInitiatedEvent(
                UUID.randomUUID().toString(),
                id,
                updatedAt,
                CorrelationId.getOrCreate(),
                new ChargeRefundInitiatedEvent.Payload(
                        organizationId,
                        environment.name(),
                        reference,
                        amount,
                        sourceSystem,
                        sourceReferenceId,
                        customerReferenceId,
                        reason.trim(),
                        updatedAt
                )
        ));
    }

    public void markRefundSubmitted(String refundReference) {
        if (status != ChargeStatus.REFUND_PENDING) {
            throw new InvalidChargeException("Only a pending refund can be submitted");
        }
        if (blank(refundReference)) {
            throw new InvalidChargeException("Provider refund reference is required");
        }
        providerRefundReference = refundReference;
        touch();
    }

    public void completeRefund(String refundReference, Money refundedAmount) {
        if (status == ChargeStatus.REFUNDED) return;
        if (status != ChargeStatus.REFUND_PENDING) {
            throw new InvalidChargeException("Only a pending refund can complete");
        }
        if (refundedAmount == null || amount.compareTo(refundedAmount) != 0
                || amount.currency() != refundedAmount.currency()) {
            throw new InvalidChargeException("Refund amount or currency does not match the charge");
        }
        if (!blank(providerRefundReference) && !blank(refundReference)
                && !providerRefundReference.equals(refundReference)) {
            throw new InvalidChargeException("Provider refund reference does not match");
        }
        if (!blank(refundReference)) providerRefundReference = refundReference;
        status = ChargeStatus.REFUNDED;
        refundedAt = ZonedDateTime.now();
        touch();
        registerEvent(new ChargeRefundedEvent(UUID.randomUUID().toString(), id, updatedAt,
                CorrelationId.getOrCreate(), new ChargeRefundedEvent.Payload(organizationId, environment.name(),
                reference, providerRefundReference, amount, sourceSystem, sourceReferenceId,
                customerReferenceId, refundedAt)));
    }

    public void failRefund(String reason) {
        if (status != ChargeStatus.REFUND_PENDING) return;
        status = ChargeStatus.SUCCESSFUL;
        failureMessage = blank(reason) ? "Provider refund failed" : reason;
        touch();
    }

    public void recordDispute(String reference, String reason) {
        if (status != ChargeStatus.SUCCESSFUL && status != ChargeStatus.REFUND_PENDING) {
            throw new InvalidChargeException("Only a collected charge can be disputed");
        }
        if (blank(reference)) throw new InvalidChargeException("Provider dispute reference is required");
        boolean newlyOpened = !"OPEN".equals(disputeStatus);
        disputeReference = reference;
        disputeStatus = "OPEN";
        disputeReason = blank(reason) ? "Provider dispute opened" : reason;
        touch();
        if (newlyOpened) registerEvent(new ChargeDisputedEvent(UUID.randomUUID().toString(), id, updatedAt,
                CorrelationId.getOrCreate(), new ChargeDisputedEvent.Payload(organizationId, environment.name(),
                this.reference, disputeReference, amount, sourceSystem, sourceReferenceId,
                customerReferenceId, disputeReason, updatedAt)));
    }

    public void resolveDispute(String reference, String resolution) {
        if (blank(disputeReference) || !disputeReference.equals(reference)) {
            throw new InvalidChargeException("Provider dispute reference does not match");
        }
        if ("RESOLVED".equals(disputeStatus)) return;
        disputeStatus = "RESOLVED";
        disputeReason = blank(resolution) ? disputeReason : resolution;
        touch();
        registerEvent(new ChargeDisputeResolvedEvent(UUID.randomUUID().toString(), id, updatedAt,
                CorrelationId.getOrCreate(), new ChargeDisputeResolvedEvent.Payload(organizationId,
                environment.name(), this.reference, disputeReference, sourceSystem, sourceReferenceId,
                customerReferenceId, disputeReason, updatedAt)));
    }

    private void validate() {
        if (id == null || organizationId == null || environment == null || blank(reference) || amount == null ||
                amount.amount().signum() <= 0 || amount.currency() == null || channel == null || provider == null ||
                providerProfileId == null || blank(sourceSystem) || blank(sourceReferenceId) || status == null ||
                expiresAt == null || createdAt == null || updatedAt == null)
            throw new InvalidChargeException("Complete valid charge data is required");
    }

    private boolean blank(String v) {
        return v == null || v.isBlank();
    }

    private void touch() {
        updatedAt = ZonedDateTime.now();
    }

    @Override
    public Long getId() {
        return id;
    }
}
