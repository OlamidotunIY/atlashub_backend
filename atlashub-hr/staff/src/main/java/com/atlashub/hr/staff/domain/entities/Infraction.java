package com.atlashub.hr.staff.domain.entities;

import com.atlashub.hr.staff.domain.events.InfractionLoggedEvent;
import com.atlashub.hr.staff.domain.events.InfractionResolvedEvent;
import com.atlashub.hr.staff.domain.exceptions.InvalidStaffOperationException;
import com.atlashub.hr.staff.domain.valueobject.InfractionSeverity;
import com.atlashub.hr.staff.domain.valueobject.InfractionStatus;
import com.atlashub.shared.domain.entities.AggregateRoot;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
public class Infraction extends AggregateRoot<Long> {

    private final Long id;
    private final Long employeeId;
    private final Long organizationId;
    private final String description;
    private final InfractionSeverity severity;
    private final ZonedDateTime reportedAt;
    private final Long reportedBy;
    private InfractionStatus status;
    private String resolutionNotes;
    private ZonedDateTime resolvedAt;

    public Infraction(
            Long id,
            Long employeeId,
            Long organizationId,
            String description,
            InfractionSeverity severity,
            ZonedDateTime reportedAt,
            Long reportedBy,
            InfractionStatus status,
            String resolutionNotes,
            ZonedDateTime resolvedAt
    ) {
        this.id = id;
        this.employeeId = employeeId;
        this.organizationId = organizationId;
        this.description = description;
        this.severity = severity;
        this.reportedAt = reportedAt;
        this.reportedBy = reportedBy;
        this.status = status;
        this.resolutionNotes = resolutionNotes;
        this.resolvedAt = resolvedAt;
    }

    public static Infraction create(
            Long id,
            Long employeeId,
            Long organizationId,
            String description,
            InfractionSeverity severity,
            Long reportedBy
    ) {
        if (id == null) {
            throw new InvalidStaffOperationException("Infraction ID cannot be null");
        }
        if (employeeId == null) {
            throw new InvalidStaffOperationException("Employee ID cannot be null");
        }
        if (organizationId == null) {
            throw new InvalidStaffOperationException("Organization ID cannot be null");
        }
        if (description == null || description.isBlank()) {
            throw new InvalidStaffOperationException("Infraction description cannot be null or blank");
        }
        if (severity == null) {
            throw new InvalidStaffOperationException("Infraction severity cannot be null");
        }
        if (reportedBy == null) {
            throw new InvalidStaffOperationException("Reported by cannot be null");
        }

        ZonedDateTime now = ZonedDateTime.now();
        Infraction infraction = new Infraction(
                id,
                employeeId,
                organizationId,
                description,
                severity,
                now,
                reportedBy,
                InfractionStatus.OPEN,
                null,
                null
        );

        infraction.registerEvent(InfractionLoggedEvent.of(
                id,
                employeeId,
                organizationId,
                description,
                severity,
                reportedBy
        ));

        return infraction;
    }

    public void resolve(String resolutionNotes) {
        if (this.status != InfractionStatus.OPEN) {
            throw new InvalidStaffOperationException("Only OPEN infractions can be resolved. Current status: " + this.status);
        }
        if (resolutionNotes == null || resolutionNotes.isBlank()) {
            throw new InvalidStaffOperationException("Resolution notes cannot be null or blank");
        }

        this.status = InfractionStatus.RESOLVED;
        this.resolutionNotes = resolutionNotes;
        this.resolvedAt = ZonedDateTime.now();

        registerEvent(InfractionResolvedEvent.of(
                this.id,
                this.employeeId,
                this.organizationId,
                this.resolutionNotes,
                this.resolvedAt
        ));
    }

    @Override
    public Long getId() {
        return id;
    }
}
