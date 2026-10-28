package com.atlashub.pay.transfers.domain.entities;

import com.atlashub.pay.transfers.domain.events.PayoutApprovedEvent;
import com.atlashub.pay.transfers.domain.events.PayoutCompletedEvent;
import com.atlashub.pay.transfers.domain.events.PayoutFailedEvent;
import com.atlashub.pay.transfers.domain.exceptions.InvalidBankDetailsException;
import com.atlashub.pay.transfers.domain.exceptions.InvalidPayoutStateException;
import com.atlashub.pay.transfers.domain.exceptions.SelfApprovalNotAllowedException;
import com.atlashub.pay.transfers.domain.valueobject.PaymentProvider;
import com.atlashub.pay.transfers.domain.valueobject.PayoutStatus;
import com.atlashub.pay.transfers.domain.valueobject.SourceSystem;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.Money;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
public class Payout extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final Money amount;
    private final String recipientBankCode;
    private final String recipientAccountNumber;
    private final String recipientAccountName;
    private final String narration;
    private PayoutStatus status;
    private final String reference;
    private final PaymentProvider provider;
    private String providerReference;
    private final SourceSystem sourceSystem;
    private final String sourceReferenceId;
    private final Long initiatedBy;
    private Long approvedBy;
    private ZonedDateTime approvedAt;
    private String failureReason;
    private final ZonedDateTime createdAt;
    private ZonedDateTime completedAt;

    public Payout(Long id, Long organizationId, Money amount, String recipientBankCode,
                  String recipientAccountNumber, String recipientAccountName, String narration,
                  PayoutStatus status, String reference, PaymentProvider provider,
                  String providerReference, SourceSystem sourceSystem, String sourceReferenceId,
                  Long initiatedBy, Long approvedBy, ZonedDateTime approvedAt,
                  String failureReason, ZonedDateTime createdAt, ZonedDateTime completedAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.amount = amount;
        this.recipientBankCode = recipientBankCode;
        this.recipientAccountNumber = recipientAccountNumber;
        this.recipientAccountName = recipientAccountName;
        this.narration = narration;
        this.status = status;
        this.reference = reference;
        this.provider = provider;
        this.providerReference = providerReference;
        this.sourceSystem = sourceSystem;
        this.sourceReferenceId = sourceReferenceId;
        this.initiatedBy = initiatedBy;
        this.approvedBy = approvedBy;
        this.approvedAt = approvedAt;
        this.failureReason = failureReason;
        this.createdAt = createdAt != null ? createdAt : ZonedDateTime.now();
        this.completedAt = completedAt;
        validateInvariants();
    }

    public static Payout create(Long id, Long organizationId, Money amount, String recipientBankCode,
                                String recipientAccountNumber, String recipientAccountName, String narration,
                                String reference, PaymentProvider provider, SourceSystem sourceSystem,
                                String sourceReferenceId, Long initiatedBy, boolean requiresApproval) {
        PayoutStatus initialStatus = requiresApproval ? PayoutStatus.PENDING_APPROVAL : PayoutStatus.APPROVED;
        Payout payout = new Payout(
                id, organizationId, amount, recipientBankCode, recipientAccountNumber,
                recipientAccountName, narration, initialStatus, reference, provider,
                null, sourceSystem, sourceReferenceId, initiatedBy,
                requiresApproval ? null : initiatedBy,
                requiresApproval ? null : ZonedDateTime.now(),
                null, ZonedDateTime.now(), null
        );

        if (!requiresApproval) {
            payout.registerEvent(new PayoutApprovedEvent(
                    UUID.randomUUID().toString(),
                    id,
                    ZonedDateTime.now(),
                    null,
                    new PayoutApprovedEvent.Payload(
                            id, organizationId, amount.amount(), amount.currency().name(),
                            initiatedBy, payout.getApprovedAt()
                    )
            ));
        }

        return payout;
    }

    public void approve(Long approverId) {
        if (approverId != null && approverId.equals(this.initiatedBy)) {
            throw new SelfApprovalNotAllowedException();
        }
        if (this.status != PayoutStatus.PENDING_APPROVAL) {
            throw new InvalidPayoutStateException("Payout cannot be approved from status: " + this.status);
        }

        this.status = PayoutStatus.APPROVED;
        this.approvedBy = approverId;
        this.approvedAt = ZonedDateTime.now();

        registerEvent(new PayoutApprovedEvent(
                UUID.randomUUID().toString(),
                this.id,
                ZonedDateTime.now(),
                null,
                new PayoutApprovedEvent.Payload(
                        this.id, this.organizationId, this.amount.amount(), this.amount.currency().name(),
                        this.approvedBy, this.approvedAt
                )
        ));
    }

    public void markProcessing() {
        if (this.status != PayoutStatus.APPROVED) {
            throw new InvalidPayoutStateException("Payout cannot be marked processing from status: " + this.status);
        }
        this.status = PayoutStatus.PROCESSING;
    }

    public void markPending(String providerReference) {
        if (this.status != PayoutStatus.PROCESSING || providerReference == null || providerReference.isBlank()) {
            throw new InvalidPayoutStateException("A processing payout requires a provider reference");
        }
        this.providerReference = providerReference;
    }

    public void complete(String providerReference) {
        if (this.status != PayoutStatus.PROCESSING) {
            throw new InvalidPayoutStateException("Payout cannot be completed from status: " + this.status);
        }
        this.providerReference = providerReference;
        this.status = PayoutStatus.SUCCESSFUL;
        this.completedAt = ZonedDateTime.now();

        registerEvent(new PayoutCompletedEvent(
                UUID.randomUUID().toString(),
                this.id,
                ZonedDateTime.now(),
                null,
                new PayoutCompletedEvent.Payload(
                        this.id, this.organizationId, this.reference, this.providerReference,
                        this.sourceSystem != null ? this.sourceSystem.name() : null,
                        this.sourceReferenceId, null, this.amount.amount(),
                        this.amount.currency().name(), this.completedAt
                )
        ));
    }

    public void fail(String failureReason) {
        if (this.status != PayoutStatus.PROCESSING && this.status != PayoutStatus.APPROVED && this.status != PayoutStatus.PENDING_APPROVAL) {
            throw new InvalidPayoutStateException("Payout cannot be failed from status: " + this.status);
        }
        this.failureReason = failureReason;
        this.status = PayoutStatus.FAILED;
        this.completedAt = ZonedDateTime.now();

        registerEvent(new PayoutFailedEvent(
                UUID.randomUUID().toString(),
                this.id,
                ZonedDateTime.now(),
                null,
                new PayoutFailedEvent.Payload(
                        this.id, this.organizationId, this.reference, this.failureReason,
                        this.sourceSystem != null ? this.sourceSystem.name() : null,
                        this.sourceReferenceId, this.amount.amount(),
                        this.amount.currency().name(), this.completedAt
                )
        ));
    }

    private void validateInvariants() {
        if (this.recipientBankCode == null || this.recipientBankCode.isBlank()
                || this.recipientAccountNumber == null || this.recipientAccountNumber.isBlank()) {
            throw new InvalidBankDetailsException();
        }
    }

    @Override
    public Long getId() {
        return this.id;
    }
}
