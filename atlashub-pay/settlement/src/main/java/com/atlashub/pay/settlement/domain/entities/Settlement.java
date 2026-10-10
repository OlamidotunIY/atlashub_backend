package com.atlashub.pay.settlement.domain.entities;

import com.atlashub.pay.settlement.domain.events.ProviderSettlementReceivedEvent;
import com.atlashub.pay.settlement.domain.events.SettlementDisputedEvent;
import com.atlashub.pay.settlement.domain.exceptions.InvalidSettlementStateException;
import com.atlashub.pay.settlement.domain.valueobject.PaymentProvider;
import com.atlashub.pay.settlement.domain.valueobject.SettlementStatus;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.CorrelationId;
import com.atlashub.shared.domain.valueobject.Money;
import com.atlashub.shared.application.security.ApiEnvironment;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Getter
public class Settlement extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final ApiEnvironment environment;
    private final PaymentProvider provider;
    private final String providerSettlementId;
    private final String providerSubaccountCode;
    private final Money grossAmount;
    private final Money netAmount;
    private final Money providerFeeAmount;
    private final Long anchorDepositAccountId;
    private final List<String> transactionReferences;
    private String anchorTransferReference;
    private final ZonedDateTime settledAt;
    private SettlementStatus status;
    private String description;
    private final ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public Settlement(Long id, Long organizationId, ApiEnvironment environment, PaymentProvider provider,
                      String providerSettlementId, String providerSubaccountCode,
                      Money grossAmount, Money netAmount, Money providerFeeAmount, Long anchorDepositAccountId,
                      List<String> transactionReferences, String anchorTransferReference,
                      ZonedDateTime settledAt, SettlementStatus status,
                      String description, ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.environment = environment;
        this.provider = provider;
        this.providerSettlementId = providerSettlementId;
        this.providerSubaccountCode = providerSubaccountCode;
        this.grossAmount = grossAmount;
        this.netAmount = netAmount;
        this.providerFeeAmount = providerFeeAmount;
        this.anchorDepositAccountId = anchorDepositAccountId;
        this.transactionReferences = transactionReferences == null ? List.of() : List.copyOf(transactionReferences);
        this.anchorTransferReference = anchorTransferReference;
        this.settledAt = settledAt;
        this.status = status;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        validateInvariants();
    }

    public static Settlement create(Long id, Long organizationId, ApiEnvironment environment, PaymentProvider provider,
                                    String providerSettlementId, String providerSubaccountCode,
                                    Money grossAmount, Money netAmount, Money providerFeeAmount,
                                    Long anchorDepositAccountId, List<String> transactionReferences,
                                    ZonedDateTime settledAt, SettlementStatus status,
                                    String description) {
        ZonedDateTime now = ZonedDateTime.now();
        return new Settlement(id, organizationId, environment, provider, providerSettlementId,
                providerSubaccountCode, grossAmount, netAmount,
                providerFeeAmount, anchorDepositAccountId, transactionReferences, null, settledAt != null ? settledAt : now,
                status != null ? status : SettlementStatus.AWAITING_ANCHOR_CREDIT, description, now, now);
    }

    @Deprecated(forRemoval = false)
    public static Settlement create(Long id, Long organizationId, PaymentProvider provider,
                                    String providerSettlementId, Money grossAmount, Money netAmount,
                                    Money providerFeeAmount, Long anchorDepositAccountId,
                                    ZonedDateTime settledAt, String description) {
        return create(id, organizationId, ApiEnvironment.LIVE, provider, providerSettlementId,
                "legacy-" + organizationId, grossAmount, netAmount, providerFeeAmount,
                anchorDepositAccountId, List.of(), settledAt, SettlementStatus.AWAITING_ANCHOR_CREDIT, description);
    }

    @Deprecated(forRemoval = false)
    public static Settlement create(Long id, Long organizationId, PaymentProvider provider,
                                    String providerSettlementId, Money grossAmount, Money netAmount,
                                    Money providerFeeAmount, Long anchorDepositAccountId,
                                    ZonedDateTime settledAt, SettlementStatus status, String description) {
        return create(id, organizationId, ApiEnvironment.LIVE, provider, providerSettlementId,
                "legacy-" + organizationId, grossAmount, netAmount, providerFeeAmount,
                anchorDepositAccountId, List.of(), settledAt, status, description);
    }

    @Deprecated(forRemoval = false)
    public Settlement(Long id, Long organizationId, PaymentProvider provider, String providerSettlementId,
                      Money grossAmount, Money netAmount, Money providerFeeAmount, Long anchorDepositAccountId,
                      String anchorTransferReference, ZonedDateTime settledAt, SettlementStatus status,
                      String description, ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this(id, organizationId, ApiEnvironment.LIVE, provider, providerSettlementId, "legacy-" + organizationId,
                grossAmount, netAmount, providerFeeAmount, anchorDepositAccountId, List.of(),
                anchorTransferReference, settledAt, status, description, createdAt, updatedAt);
    }

    public void confirm(String anchorTransferReference) {
        if (this.status == SettlementStatus.CONFIRMED) {
            return;
        }
        if (this.status != SettlementStatus.AWAITING_ANCHOR_CREDIT) {
            throw new InvalidSettlementStateException("Cannot confirm settlement with status: " + this.status);
        }
        if (anchorTransferReference == null || anchorTransferReference.isBlank()) {
            throw new InvalidSettlementStateException("Anchor transfer reference is required for confirmation");
        }
        this.anchorTransferReference = anchorTransferReference.trim();
        this.status = SettlementStatus.CONFIRMED;
        touch();
        registerEvent(new ProviderSettlementReceivedEvent(UUID.randomUUID().toString(), this.id, this.updatedAt,
                CorrelationId.getOrCreate(),
                new ProviderSettlementReceivedEvent.Payload(this.organizationId, this.environment.name(),
                        this.provider.name(), this.providerSettlementId, this.netAmount, this.settledAt)));
    }

    public void markProviderConfirmed() {
        if (this.status == SettlementStatus.AWAITING_ANCHOR_CREDIT) {
            return;
        }
        if (this.status != SettlementStatus.PROVIDER_PENDING) {
            throw new InvalidSettlementStateException(
                    "Only provider-pending settlements can be marked provider confirmed");
        }
        this.status = SettlementStatus.AWAITING_ANCHOR_CREDIT;
        touch();
    }

    public void requireReconciliation(String reason) {
        if (this.status == SettlementStatus.CONFIRMED || this.status == SettlementStatus.FAILED) {
            throw new InvalidSettlementStateException(
                    "Cannot require reconciliation for settlement in status: " + this.status);
        }
        this.status = SettlementStatus.RECONCILIATION_REQUIRED;
        if (reason != null && !reason.isBlank()) {
            this.description = this.description != null && !this.description.isBlank() ?
                    this.description + " [Reconciliation reason: " + reason.trim() + "]" : reason.trim();
        }
        touch();
    }

    public void dispute(String reason) {
        if (this.status == SettlementStatus.DISPUTED) {
            return;
        }
        if (this.status == SettlementStatus.FAILED) {
            throw new InvalidSettlementStateException("Cannot dispute a failed settlement");
        }
        if (reason == null || reason.isBlank()) {
            throw new InvalidSettlementStateException("Dispute reason is required");
        }
        this.status = SettlementStatus.DISPUTED;
        if (this.description != null && !this.description.isBlank()) {
            this.description = this.description + " [Dispute: " + reason.trim() + "]";
        } else {
            this.description = reason.trim();
        }
        touch();
        registerEvent(new SettlementDisputedEvent(UUID.randomUUID().toString(), this.id, this.updatedAt,
                CorrelationId.getOrCreate(),
                new SettlementDisputedEvent.Payload(this.organizationId, this.provider, this.providerSettlementId,
                        this.netAmount, reason.trim(), this.updatedAt)));
    }

    public void resolveDispute() {
        if (this.status != SettlementStatus.DISPUTED) {
            throw new InvalidSettlementStateException("Only disputed settlements can be resolved");
        }
        this.status = this.anchorTransferReference != null &&
                !this.anchorTransferReference.isBlank() ? SettlementStatus.CONFIRMED : SettlementStatus.AWAITING_ANCHOR_CREDIT;
        touch();
    }

    public void retryReconciliation() {
        if (status != SettlementStatus.RECONCILIATION_REQUIRED) {
            throw new InvalidSettlementStateException("Only reconciliation-required settlements can be retried");
        }
        status = SettlementStatus.AWAITING_ANCHOR_CREDIT;
        touch();
    }

    public void fail(String reason) {
        if (this.status == SettlementStatus.FAILED) {
            return;
        }
        if (this.status == SettlementStatus.CONFIRMED) {
            throw new InvalidSettlementStateException("A confirmed settlement cannot be marked as failed");
        }
        this.status = SettlementStatus.FAILED;
        if (reason != null && !reason.isBlank()) {
            this.description = this.description != null && !this.description.isBlank() ?
                    this.description + " [Failure reason: " + reason.trim() + "]" : reason.trim();
        }
        touch();
    }

    public boolean isConfirmed() {
        return this.status == SettlementStatus.CONFIRMED;
    }

    public boolean isDisputed() {
        return this.status == SettlementStatus.DISPUTED;
    }

    public boolean isAwaitingAnchorCredit() {
        return this.status == SettlementStatus.AWAITING_ANCHOR_CREDIT;
    }

    public boolean isReconciliationRequired() {
        return this.status == SettlementStatus.RECONCILIATION_REQUIRED;
    }

    private void validateInvariants() {
        if (this.id == null || this.organizationId == null || this.environment == null) {
            throw new InvalidSettlementStateException("Settlement id and organizationId cannot be null");
        }
        if (this.provider == null) {
            throw new InvalidSettlementStateException("Payment provider is required");
        }
        if (this.providerSettlementId == null || this.providerSettlementId.isBlank()) {
            throw new InvalidSettlementStateException("Provider settlement id is required");
        }
        if (this.providerSubaccountCode == null || this.providerSubaccountCode.isBlank()) {
            throw new InvalidSettlementStateException("Provider subaccount code is required");
        }
        if (this.grossAmount == null || this.netAmount == null || this.providerFeeAmount == null) {
            throw new InvalidSettlementStateException("Gross amount, net amount, and fee amount are required");
        }
        if (this.grossAmount.amount().signum() < 0 || this.netAmount.amount().signum() < 0 ||
                this.providerFeeAmount.amount().signum() < 0) {
            throw new InvalidSettlementStateException("Settlement amounts cannot be negative");
        }
        if (this.grossAmount.currency() != this.netAmount.currency() ||
                this.grossAmount.currency() != this.providerFeeAmount.currency()) {
            throw new InvalidSettlementStateException(
                    "Currency mismatch between gross amount, net amount, and fee amount");
        }
        if (this.grossAmount.amount().subtract(this.providerFeeAmount.amount()).compareTo(this.netAmount.amount()) !=
                0) {
            throw new InvalidSettlementStateException("Gross amount minus provider fee must equal net amount");
        }
        if (this.anchorDepositAccountId == null) {
            throw new InvalidSettlementStateException("Anchor deposit account id is required");
        }
        if (this.status == null) {
            throw new InvalidSettlementStateException("Settlement status cannot be null");
        }
        if (this.settledAt == null) {
            throw new InvalidSettlementStateException("Settled at timestamp cannot be null");
        }
        if (this.createdAt == null || this.updatedAt == null) {
            throw new InvalidSettlementStateException("Created and updated timestamps cannot be null");
        }
    }

    private void touch() {
        this.updatedAt = ZonedDateTime.now();
    }

    @Override
    public Long getId() {
        return id;
    }
}
