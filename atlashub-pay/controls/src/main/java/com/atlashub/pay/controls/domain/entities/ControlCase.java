package com.atlashub.pay.controls.domain.entities;

import com.atlashub.pay.controls.domain.events.ControlCaseResolvedEvent;
import com.atlashub.pay.controls.domain.exceptions.InvalidControlTransitionException;
import com.atlashub.pay.controls.domain.valueobject.ControlCaseStatus;
import com.atlashub.pay.controls.domain.valueobject.EvidenceRequirement;
import com.atlashub.shared.domain.entities.AggregateRoot;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Getter
public class ControlCase extends AggregateRoot<Long> {

    private final Long id;
    private final Long decisionId;
    private final Long organizationId;
    private ControlCaseStatus status;
    private List<EvidenceRequirement> requestedEvidence;
    private List<String> submittedEvidenceRefs;
    private final ZonedDateTime openedAt;
    private Long resolvedBy;
    private ZonedDateTime resolvedAt;
    private String resolutionReason;

    public ControlCase(Long id, Long decisionId, Long organizationId, ControlCaseStatus status,
                       List<EvidenceRequirement> requestedEvidence, List<String> submittedEvidenceRefs,
                       ZonedDateTime openedAt, Long resolvedBy, ZonedDateTime resolvedAt,
                       String resolutionReason) {
        this.id = id;
        this.decisionId = decisionId;
        this.organizationId = organizationId;
        this.status = status;
        this.requestedEvidence = requestedEvidence != null ? new ArrayList<>(requestedEvidence) : new ArrayList<>();
        this.submittedEvidenceRefs = submittedEvidenceRefs != null ? new ArrayList<>(submittedEvidenceRefs) : new ArrayList<>();
        this.openedAt = openedAt;
        this.resolvedBy = resolvedBy;
        this.resolvedAt = resolvedAt;
        this.resolutionReason = resolutionReason;
    }

    public static ControlCase open(Long id, Long decisionId, Long organizationId,
                                   List<EvidenceRequirement> requestedEvidence) {
        if (id == null || decisionId == null || organizationId == null) {
            throw new InvalidControlTransitionException("Id, decisionId, and organizationId must not be null");
        }
        return new ControlCase(
                id,
                decisionId,
                organizationId,
                requestedEvidence != null && !requestedEvidence.isEmpty() ? ControlCaseStatus.EVIDENCE_REQUESTED : ControlCaseStatus.OPEN,
                requestedEvidence != null ? requestedEvidence : Collections.emptyList(),
                Collections.emptyList(),
                ZonedDateTime.now(),
                null,
                null,
                null
        );
    }

    public void requestEvidence(List<EvidenceRequirement> requirements) {
        if (this.status == ControlCaseStatus.CLOSED || this.status == ControlCaseStatus.RELEASED || this.status == ControlCaseStatus.REJECTED) {
            throw new InvalidControlTransitionException("Cannot request evidence on resolved case: " + this.status);
        }
        if (requirements != null) {
            this.requestedEvidence.addAll(requirements);
        }
        this.status = ControlCaseStatus.EVIDENCE_REQUESTED;
    }

    public void submitEvidence(String evidenceRef) {
        if (this.status == ControlCaseStatus.CLOSED || this.status == ControlCaseStatus.RELEASED || this.status == ControlCaseStatus.REJECTED) {
            throw new InvalidControlTransitionException("Cannot submit evidence on resolved case: " + this.status);
        }
        if (evidenceRef != null && !evidenceRef.isBlank()) {
            this.submittedEvidenceRefs.add(evidenceRef);
        }
        this.status = ControlCaseStatus.UNDER_REVIEW;
    }

    public void approveRelease(Long reviewerId, String reason) {
        if (this.status == ControlCaseStatus.CLOSED) {
            throw new InvalidControlTransitionException("Cannot approve closed case");
        }
        this.status = ControlCaseStatus.RELEASED;
        this.resolvedBy = reviewerId;
        this.resolutionReason = reason;
        this.resolvedAt = ZonedDateTime.now();

        registerEvent(new ControlCaseResolvedEvent(
                UUID.randomUUID().toString(),
                this.id,
                this.resolvedAt,
                UUID.randomUUID().toString(),
                new ControlCaseResolvedEvent.Payload(
                        this.id,
                        this.decisionId,
                        this.organizationId,
                        this.status.name(),
                        this.resolvedBy,
                        this.resolutionReason,
                        this.resolvedAt
                )
        ));
    }

    public void rejectRelease(Long reviewerId, String reason) {
        if (this.status == ControlCaseStatus.CLOSED) {
            throw new InvalidControlTransitionException("Cannot reject closed case");
        }
        this.status = ControlCaseStatus.REJECTED;
        this.resolvedBy = reviewerId;
        this.resolutionReason = reason;
        this.resolvedAt = ZonedDateTime.now();

        registerEvent(new ControlCaseResolvedEvent(
                UUID.randomUUID().toString(),
                this.id,
                this.resolvedAt,
                UUID.randomUUID().toString(),
                new ControlCaseResolvedEvent.Payload(
                        this.id,
                        this.decisionId,
                        this.organizationId,
                        this.status.name(),
                        this.resolvedBy,
                        this.resolutionReason,
                        this.resolvedAt
                )
        ));
    }

    public void close() {
        this.status = ControlCaseStatus.CLOSED;
    }

    @Override
    public Long getId() {
        return id;
    }
}
