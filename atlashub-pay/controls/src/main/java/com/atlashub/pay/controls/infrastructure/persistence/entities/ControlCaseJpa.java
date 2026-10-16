package com.atlashub.pay.controls.infrastructure.persistence.entities;

import com.atlashub.pay.controls.domain.valueobject.ControlCaseStatus;
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
        name = "pay_control_cases",
        indexes = {
                @Index(name = "idx_case_decision", columnList = "decision_id", unique = true),
                @Index(name = "idx_case_org_status_opened", columnList = "organization_id, status, opened_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ControlCaseJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "decision_id", nullable = false)
    private Long decisionId;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ControlCaseStatus status;

    @Column(name = "requested_evidence", columnDefinition = "json")
    private String requestedEvidence;

    @Column(name = "submitted_evidence_refs", columnDefinition = "json")
    private String submittedEvidenceRefs;

    @Column(name = "opened_at", nullable = false)
    private ZonedDateTime openedAt;

    @Column(name = "resolved_by")
    private Long resolvedBy;

    @Column(name = "resolved_at")
    private ZonedDateTime resolvedAt;

    @Column(name = "resolution_reason", length = 500)
    private String resolutionReason;

    @Version
    private Long version;
}
