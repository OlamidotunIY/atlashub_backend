package com.atlashub.pay.controls.infrastructure.persistence.entities;

import com.atlashub.pay.controls.domain.valueobject.ControlAction;
import com.atlashub.pay.controls.domain.valueobject.ControlStatus;
import com.atlashub.pay.controls.domain.valueobject.PaymentContext;
import com.atlashub.pay.controls.domain.valueobject.PaymentRail;
import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Table(
        name = "pay_payment_control_decisions",
        indexes = {
                @Index(name = "idx_decision_ref_policy", columnList = "payment_instruction_reference, policy_id", unique = true),
                @Index(name = "idx_decision_provider_ref", columnList = "provider_control_reference"),
                @Index(name = "idx_decision_org_status", columnList = "organization_id, status")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PaymentControlDecisionJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "policy_id", nullable = false)
    private Long policyId;

    @Column(name = "facility_id", nullable = false)
    private Long facilityId;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "payment_instruction_reference", nullable = false, length = 100)
    private String paymentInstructionReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_rail", nullable = false, length = 50)
    private PaymentRail paymentRail;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_context", nullable = false, length = 50)
    private PaymentContext paymentContext;

    @Enumerated(EnumType.STRING)
    @Column(name = "evaluated_action", nullable = false, length = 30)
    private ControlAction evaluatedAction;

    @Enumerated(EnumType.STRING)
    @Column(name = "enforced_action", nullable = false, length = 30)
    private ControlAction enforcedAction;

    @Column(name = "reason_codes", columnDefinition = "json")
    private String reasonCodes;

    @Column(name = "risk_signal_id")
    private Long riskSignalId;

    @Column(name = "provider_control_reference", length = 150)
    private String providerControlReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ControlStatus status;

    @Column(name = "evaluated_at", nullable = false)
    private ZonedDateTime evaluatedAt;

    @Column(name = "applied_at")
    private ZonedDateTime appliedAt;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Version
    private Long version;
}
