package com.atlashub.pay.controls.domain.entities;

import com.atlashub.pay.controls.domain.events.ControlledFundsFrozenEvent;
import com.atlashub.pay.controls.domain.events.ControlledFundsUnfrozenEvent;
import com.atlashub.pay.controls.domain.events.PaymentBlockedEvent;
import com.atlashub.pay.controls.domain.events.PaymentControlEvaluatedEvent;
import com.atlashub.pay.controls.domain.events.PaymentHeldEvent;
import com.atlashub.pay.controls.domain.exceptions.InvalidControlTransitionException;
import com.atlashub.pay.controls.domain.exceptions.InvalidDecisionException;
import com.atlashub.pay.controls.domain.valueobject.ControlAction;
import com.atlashub.pay.controls.domain.valueobject.ControlReason;
import com.atlashub.pay.controls.domain.valueobject.ControlStatus;
import com.atlashub.pay.controls.domain.valueobject.PaymentContext;
import com.atlashub.pay.controls.domain.valueobject.PaymentRail;
import com.atlashub.shared.domain.entities.AggregateRoot;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
public class PaymentControlDecision extends AggregateRoot<Long> {

    private final Long id;
    private final Long policyId;
    private final Long facilityId;
    private final Long organizationId;
    private final String paymentInstructionReference;
    private final PaymentRail paymentRail;
    private final PaymentContext paymentContext;
    private final ControlAction evaluatedAction;
    private ControlAction enforcedAction;
    private final Set<ControlReason> reasonCodes;
    private final Long riskSignalId;
    private String providerControlReference;
    private ControlStatus status;
    private final ZonedDateTime evaluatedAt;
    private ZonedDateTime appliedAt;
    private String failureReason;

    public PaymentControlDecision(Long id, Long policyId, Long facilityId, Long organizationId,
                                  String paymentInstructionReference, PaymentRail paymentRail,
                                  PaymentContext paymentContext, ControlAction evaluatedAction,
                                  ControlAction enforcedAction, Set<ControlReason> reasonCodes,
                                  Long riskSignalId, String providerControlReference,
                                  ControlStatus status, ZonedDateTime evaluatedAt,
                                  ZonedDateTime appliedAt, String failureReason) {
        this.id = id;
        this.policyId = policyId;
        this.facilityId = facilityId;
        this.organizationId = organizationId;
        this.paymentInstructionReference = paymentInstructionReference;
        this.paymentRail = paymentRail;
        this.paymentContext = paymentContext;
        this.evaluatedAction = evaluatedAction;
        this.enforcedAction = enforcedAction;
        this.reasonCodes = reasonCodes != null ? new HashSet<>(reasonCodes) : new HashSet<>();
        this.riskSignalId = riskSignalId;
        this.providerControlReference = providerControlReference;
        this.status = status;
        this.evaluatedAt = evaluatedAt;
        this.appliedAt = appliedAt;
        this.failureReason = failureReason;
    }

    public static PaymentControlDecision evaluate(Long id, Long policyId, Long facilityId, Long organizationId,
                                                   String paymentInstructionReference, PaymentRail paymentRail,
                                                   PaymentContext paymentContext, ControlAction evaluatedAction,
                                                   ControlAction enforcedAction, Set<ControlReason> reasonCodes,
                                                   Long riskSignalId) {
        if (id == null) {
            throw new InvalidDecisionException("Decision ID must not be null");
        }
        if (policyId == null) {
            throw new InvalidDecisionException("Policy ID must not be null");
        }
        if (facilityId == null) {
            throw new InvalidDecisionException("Facility ID must not be null");
        }
        if (organizationId == null) {
            throw new InvalidDecisionException("Organization ID must not be null");
        }
        if (paymentInstructionReference == null || paymentInstructionReference.isBlank()) {
            throw new InvalidDecisionException("Payment instruction reference must not be blank");
        }
        if (paymentRail == null) {
            throw new InvalidDecisionException("Payment rail must not be null");
        }
        if (paymentContext == null) {
            throw new InvalidDecisionException("Payment context must not be null");
        }
        if (evaluatedAction == null) {
            throw new InvalidDecisionException("Evaluated action must not be null");
        }
        if (enforcedAction == null) {
            throw new InvalidDecisionException("Enforced action must not be null");
        }

        ZonedDateTime now = ZonedDateTime.now();
        PaymentControlDecision decision = new PaymentControlDecision(
                id,
                policyId,
                facilityId,
                organizationId,
                paymentInstructionReference,
                paymentRail,
                paymentContext,
                evaluatedAction,
                enforcedAction,
                reasonCodes != null ? reasonCodes : Collections.emptySet(),
                riskSignalId,
                null,
                enforcedAction == ControlAction.ALLOW ? ControlStatus.APPLIED : ControlStatus.EVALUATED,
                now,
                enforcedAction == ControlAction.ALLOW ? now : null,
                null
        );

        Set<String> reasons = decision.reasonCodes.stream().map(Enum::name).collect(Collectors.toSet());
        decision.registerEvent(new PaymentControlEvaluatedEvent(
                UUID.randomUUID().toString(),
                id,
                now,
                UUID.randomUUID().toString(),
                new PaymentControlEvaluatedEvent.Payload(
                        id,
                        policyId,
                        facilityId,
                        organizationId,
                        paymentInstructionReference,
                        evaluatedAction.name(),
                        enforcedAction.name(),
                        reasons,
                        now
                )
        ));

        return decision;
    }

    public void markProviderPending(String providerRef) {
        if (this.status != ControlStatus.EVALUATED && this.status != ControlStatus.PENDING_PROVIDER) {
            throw new InvalidControlTransitionException("Cannot mark provider pending from status: " + this.status);
        }
        this.status = ControlStatus.PENDING_PROVIDER;
        this.providerControlReference = providerRef;
    }

    public void confirmApplied(String providerRef) {
        this.status = ControlStatus.APPLIED;
        if (providerRef != null) {
            this.providerControlReference = providerRef;
        }
        this.appliedAt = ZonedDateTime.now();

        if (this.enforcedAction == ControlAction.HOLD) {
            registerEvent(new PaymentHeldEvent(
                    UUID.randomUUID().toString(),
                    this.id,
                    this.appliedAt,
                    UUID.randomUUID().toString(),
                    new PaymentHeldEvent.Payload(
                            this.id,
                            this.policyId,
                            this.facilityId,
                            this.organizationId,
                            this.paymentInstructionReference,
                            this.providerControlReference,
                            this.appliedAt
                    )
            ));
        } else if (this.enforcedAction == ControlAction.BLOCK) {
            registerEvent(new PaymentBlockedEvent(
                    UUID.randomUUID().toString(),
                    this.id,
                    this.appliedAt,
                    UUID.randomUUID().toString(),
                    new PaymentBlockedEvent.Payload(
                            this.id,
                            this.policyId,
                            this.facilityId,
                            this.organizationId,
                            this.paymentInstructionReference,
                            "Blocked according to controlled funds policy",
                            this.appliedAt
                    )
            ));
        } else if (this.enforcedAction == ControlAction.FREEZE) {
            registerEvent(new ControlledFundsFrozenEvent(
                    UUID.randomUUID().toString(),
                    this.id,
                    this.appliedAt,
                    UUID.randomUUID().toString(),
                    new ControlledFundsFrozenEvent.Payload(
                            this.id,
                            this.facilityId,
                            this.organizationId,
                            this.paymentInstructionReference,
                            BigDecimal.ZERO,
                            "NGN",
                            this.appliedAt
                    )
            ));
        }
    }

    public void fail(String reason) {
        this.status = ControlStatus.FAILED;
        this.failureReason = reason;
    }

    public void release() {
        if (this.status != ControlStatus.APPLIED && this.status != ControlStatus.PENDING_PROVIDER && this.status != ControlStatus.EVALUATED) {
            throw new InvalidControlTransitionException("Cannot release decision in status: " + this.status);
        }
        this.status = ControlStatus.RELEASED;

        if (this.enforcedAction == ControlAction.FREEZE) {
            registerEvent(new ControlledFundsUnfrozenEvent(
                    UUID.randomUUID().toString(),
                    this.id,
                    ZonedDateTime.now(),
                    UUID.randomUUID().toString(),
                    new ControlledFundsUnfrozenEvent.Payload(
                            this.id,
                            this.facilityId,
                            this.organizationId,
                            this.paymentInstructionReference,
                            BigDecimal.ZERO,
                            "NGN",
                            ZonedDateTime.now()
                    )
            ));
        }
    }

    public void expire() {
        this.status = ControlStatus.EXPIRED;
    }

    @Override
    public Long getId() {
        return id;
    }
}
