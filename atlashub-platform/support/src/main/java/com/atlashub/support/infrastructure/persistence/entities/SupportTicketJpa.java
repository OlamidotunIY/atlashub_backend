package com.atlashub.support.infrastructure.persistence.entities;

import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import com.atlashub.support.domain.valueobject.TicketCategory;
import com.atlashub.support.domain.valueobject.TicketPriority;
import com.atlashub.support.domain.valueobject.TicketStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.ZonedDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "support_tickets",
        indexes = {
                @Index(name = "idx_ticket_number", columnList = "ticket_number", unique = true),
                @Index(name = "idx_ticket_org_status", columnList = "organization_id, status"),
                @Index(name = "idx_ticket_assigned_status", columnList = "assigned_to, status"),
                @Index(name = "idx_ticket_status_sla", columnList = "status, sla_breached"),
                @Index(name = "idx_ticket_resolution_deadline", columnList = "resolution_deadline")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SupportTicketJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "ticket_number", nullable = false, unique = true, length = 32)
    private String ticketNumber;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "raised_by", nullable = false)
    private Long raisedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 32)
    private TicketCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 16)
    private TicketPriority priority;

    @Column(name = "subject", nullable = false, length = 255)
    private String subject;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private TicketStatus status;

    @Column(name = "assigned_to")
    private Long assignedTo;

    @Column(name = "sla_breached", nullable = false)
    private boolean slaBreached;

    @Column(name = "related_entity_type", length = 64)
    private String relatedEntityType;

    @Column(name = "related_entity_id", length = 64)
    private String relatedEntityId;

    @Column(name = "first_response_deadline", nullable = false)
    private ZonedDateTime firstResponseDeadline;

    @Column(name = "resolution_deadline", nullable = false)
    private ZonedDateTime resolutionDeadline;

    @Column(name = "first_responded_at")
    private ZonedDateTime firstRespondedAt;

    @Column(name = "resolved_at")
    private ZonedDateTime resolvedAt;

    @Column(name = "resolution_note", length = 1024)
    private String resolutionNote;

    @Column(name = "closed_at")
    private ZonedDateTime closedAt;

    @Column(name = "closed_by")
    private Long closedBy;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version;
}
