package com.atlashub.hr.staff.infrastructure.persistence.entities;

import com.atlashub.hr.staff.domain.valueobject.InfractionSeverity;
import com.atlashub.hr.staff.domain.valueobject.InfractionStatus;
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
import lombok.Setter;

import java.time.ZonedDateTime;

@Entity
@Table(
        name = "hr_infractions",
        indexes = {
                @Index(name = "idx_infraction_org_emp", columnList = "organization_id, employee_id"),
                @Index(name = "idx_infraction_org_status", columnList = "organization_id, status")
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class InfractionJpaEntity implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(nullable = false, length = 512)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private InfractionSeverity severity;

    @Column(name = "reported_at", nullable = false)
    private ZonedDateTime reportedAt;

    @Column(name = "reported_by", nullable = false)
    private Long reportedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private InfractionStatus status;

    @Column(name = "resolution_notes", length = 512)
    private String resolutionNotes;

    @Column(name = "resolved_at")
    private ZonedDateTime resolvedAt;

    @Version
    private Long version;
}
