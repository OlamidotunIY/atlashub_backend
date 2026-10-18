package com.atlashub.support.domain.entities;

import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.support.domain.events.TicketAssignedEvent;
import com.atlashub.support.domain.events.TicketClosedEvent;
import com.atlashub.support.domain.events.TicketCreatedEvent;
import com.atlashub.support.domain.events.TicketEscalatedEvent;
import com.atlashub.support.domain.events.TicketMessageAddedEvent;
import com.atlashub.support.domain.events.TicketReopenedEvent;
import com.atlashub.support.domain.events.TicketResolvedEvent;
import com.atlashub.support.domain.events.TicketSlaBreachedEvent;
import com.atlashub.support.domain.exceptions.InvalidTicketStateException;
import com.atlashub.support.domain.exceptions.SupportInvariantException;
import com.atlashub.support.domain.exceptions.TicketAlreadyClosedException;
import com.atlashub.support.domain.valueobject.AuthorType;
import com.atlashub.support.domain.valueobject.TicketCategory;
import com.atlashub.support.domain.valueobject.TicketPriority;
import com.atlashub.support.domain.valueobject.TicketStatus;
import java.time.ZonedDateTime;
import lombok.Getter;

@Getter
public class SupportTicket extends AggregateRoot<Long> {

    private final Long id;
    private final String ticketNumber;
    private final Long organizationId;
    private final Long raisedBy;
    private final TicketCategory category;
    private final TicketPriority priority;
    private final String subject;
    private TicketStatus status;
    private Long assignedTo;
    private boolean slaBreached;
    private final String relatedEntityType;
    private final String relatedEntityId;
    private final ZonedDateTime firstResponseDeadline;
    private final ZonedDateTime resolutionDeadline;
    private ZonedDateTime firstRespondedAt;
    private ZonedDateTime resolvedAt;
    private String resolutionNote;
    private ZonedDateTime closedAt;
    private Long closedBy;
    private final ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public SupportTicket(
            Long id,
            String ticketNumber,
            Long organizationId,
            Long raisedBy,
            TicketCategory category,
            TicketPriority priority,
            String subject,
            TicketStatus status,
            Long assignedTo,
            boolean slaBreached,
            String relatedEntityType,
            String relatedEntityId,
            ZonedDateTime firstResponseDeadline,
            ZonedDateTime resolutionDeadline,
            ZonedDateTime firstRespondedAt,
            ZonedDateTime resolvedAt,
            String resolutionNote,
            ZonedDateTime closedAt,
            Long closedBy,
            ZonedDateTime createdAt,
            ZonedDateTime updatedAt
    ) {
        this.id = id;
        this.ticketNumber = ticketNumber;
        this.organizationId = organizationId;
        this.raisedBy = raisedBy;
        this.category = category;
        this.priority = priority;
        this.subject = subject;
        this.status = status;
        this.assignedTo = assignedTo;
        this.slaBreached = slaBreached;
        this.relatedEntityType = relatedEntityType;
        this.relatedEntityId = relatedEntityId;
        this.firstResponseDeadline = firstResponseDeadline;
        this.resolutionDeadline = resolutionDeadline;
        this.firstRespondedAt = firstRespondedAt;
        this.resolvedAt = resolvedAt;
        this.resolutionNote = resolutionNote;
        this.closedAt = closedAt;
        this.closedBy = closedBy;
        this.createdAt = createdAt != null ? createdAt : ZonedDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : ZonedDateTime.now();
    }

    public static SupportTicket create(
            Long id,
            String ticketNumber,
            Long organizationId,
            Long raisedBy,
            TicketCategory category,
            TicketPriority priority,
            String subject,
            String relatedEntityType,
            String relatedEntityId,
            int firstResponseMinutes,
            int resolutionMinutes,
            String correlationId
    ) {
        if (id == null) {
            throw new SupportInvariantException("Support ticket ID cannot be null");
        }
        if (ticketNumber == null || ticketNumber.isBlank()) {
            throw new SupportInvariantException("Ticket number cannot be blank");
        }
        if (organizationId == null) {
            throw new SupportInvariantException("Organization ID cannot be null");
        }
        if (raisedBy == null) {
            throw new SupportInvariantException("Raised by user ID cannot be null");
        }
        if (category == null) {
            throw new SupportInvariantException("Ticket category cannot be null");
        }
        if (priority == null) {
            throw new SupportInvariantException("Ticket priority cannot be null");
        }
        if (subject == null || subject.isBlank()) {
            throw new SupportInvariantException("Ticket subject cannot be blank");
        }

        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime firstResponseDeadline = now.plusMinutes(firstResponseMinutes);
        ZonedDateTime resolutionDeadline = now.plusMinutes(resolutionMinutes);

        SupportTicket ticket = new SupportTicket(
                id,
                ticketNumber.trim(),
                organizationId,
                raisedBy,
                category,
                priority,
                subject.trim(),
                TicketStatus.OPEN,
                null,
                false,
                relatedEntityType,
                relatedEntityId,
                firstResponseDeadline,
                resolutionDeadline,
                null,
                null,
                null,
                null,
                null,
                now,
                now
        );

        ticket.registerEvent(TicketCreatedEvent.of(
                id,
                correlationId,
                new TicketCreatedEvent.Payload(
                        id,
                        ticket.getTicketNumber(),
                        organizationId,
                        raisedBy,
                        category,
                        priority,
                        ticket.getSubject()
                )
        ));

        return ticket;
    }

    public void assign(Long agentId, String correlationId) {
        if (agentId == null) {
            throw new SupportInvariantException("Agent ID cannot be null when assigning ticket");
        }
        if (this.status == TicketStatus.CLOSED) {
            throw new InvalidTicketStateException("Cannot assign a closed ticket");
        }

        this.assignedTo = agentId;
        if (this.status == TicketStatus.OPEN) {
            this.status = TicketStatus.ASSIGNED;
        }
        recordFirstResponse();
        this.updatedAt = ZonedDateTime.now();

        registerEvent(TicketAssignedEvent.of(
                this.id,
                correlationId,
                new TicketAssignedEvent.Payload(this.id, agentId, this.organizationId)
        ));
    }

    public void startProgress() {
        if (this.status != TicketStatus.ASSIGNED && this.status != TicketStatus.PENDING_CUSTOMER_RESPONSE) {
            throw new InvalidTicketStateException("Cannot start progress on ticket in status: " + this.status);
        }
        this.status = TicketStatus.IN_PROGRESS;
        this.updatedAt = ZonedDateTime.now();
    }

    public void awaitCustomerResponse() {
        if (this.status != TicketStatus.IN_PROGRESS) {
            throw new InvalidTicketStateException("Cannot await customer response on ticket in status: " + this.status);
        }
        this.status = TicketStatus.PENDING_CUSTOMER_RESPONSE;
        this.updatedAt = ZonedDateTime.now();
    }

    public void resume() {
        if (this.status != TicketStatus.PENDING_CUSTOMER_RESPONSE) {
            throw new InvalidTicketStateException("Cannot resume ticket that is not pending customer response");
        }
        this.status = TicketStatus.IN_PROGRESS;
        this.updatedAt = ZonedDateTime.now();
    }

    public void resolve(Long agentId, String note, String correlationId) {
        if (this.status != TicketStatus.IN_PROGRESS) {
            throw new InvalidTicketStateException("Ticket must be IN_PROGRESS before it can be resolved. Current status: " + this.status);
        }
        if (note == null || note.isBlank()) {
            throw new SupportInvariantException("Resolution note is required when resolving a ticket");
        }

        this.status = TicketStatus.RESOLVED;
        this.resolvedAt = ZonedDateTime.now();
        this.resolutionNote = note.trim();
        this.updatedAt = ZonedDateTime.now();

        registerEvent(TicketResolvedEvent.of(
                this.id,
                correlationId,
                new TicketResolvedEvent.Payload(this.id, agentId, this.resolutionNote, this.organizationId)
        ));
    }

    public void close(Long closedByActor, String correlationId) {
        if (this.status == TicketStatus.CLOSED) {
            throw new TicketAlreadyClosedException(this.id);
        }

        this.status = TicketStatus.CLOSED;
        this.closedAt = ZonedDateTime.now();
        this.closedBy = closedByActor;
        this.updatedAt = ZonedDateTime.now();

        registerEvent(TicketClosedEvent.of(
                this.id,
                correlationId,
                new TicketClosedEvent.Payload(this.id, closedByActor, this.organizationId)
        ));
    }

    public void reopen(Long userId, String reason, String correlationId) {
        if (this.status != TicketStatus.CLOSED && this.status != TicketStatus.RESOLVED) {
            throw new InvalidTicketStateException("Only CLOSED or RESOLVED tickets can be reopened. Current: " + this.status);
        }
        if (reason == null || reason.isBlank()) {
            throw new SupportInvariantException("Reopen reason is required");
        }

        this.status = TicketStatus.OPEN;
        this.resolvedAt = null;
        this.resolutionNote = null;
        this.closedAt = null;
        this.closedBy = null;
        this.updatedAt = ZonedDateTime.now();

        registerEvent(TicketReopenedEvent.of(
                this.id,
                correlationId,
                new TicketReopenedEvent.Payload(this.id, userId, reason.trim(), this.organizationId)
        ));
    }

    public void escalate(Long newAgentId, String reason, String correlationId) {
        if (this.status == TicketStatus.CLOSED) {
            throw new InvalidTicketStateException("Cannot escalate a closed ticket");
        }
        if (newAgentId == null) {
            throw new SupportInvariantException("New agent ID is required for escalation");
        }
        if (reason == null || reason.isBlank()) {
            throw new SupportInvariantException("Escalation reason is required");
        }

        this.assignedTo = newAgentId;
        this.updatedAt = ZonedDateTime.now();

        registerEvent(TicketEscalatedEvent.of(
                this.id,
                correlationId,
                new TicketEscalatedEvent.Payload(this.id, newAgentId, reason.trim(), this.organizationId)
        ));
    }

    public void recordFirstResponse() {
        if (this.firstRespondedAt == null) {
            this.firstRespondedAt = ZonedDateTime.now();
            this.updatedAt = ZonedDateTime.now();
        }
    }

    public void markSlaBreached(String breachType, String correlationId) {
        if (!this.slaBreached) {
            this.slaBreached = true;
            this.updatedAt = ZonedDateTime.now();

            registerEvent(TicketSlaBreachedEvent.of(
                    this.id,
                    correlationId,
                    new TicketSlaBreachedEvent.Payload(
                            this.id,
                            this.ticketNumber,
                            this.organizationId,
                            this.priority,
                            breachType
                    )
            ));
        }
    }

    public void recordMessageAdded(Long authorId, AuthorType authorType, boolean isInternalNote, String correlationId) {
        this.updatedAt = ZonedDateTime.now();
        registerEvent(TicketMessageAddedEvent.of(
                this.id,
                correlationId,
                new TicketMessageAddedEvent.Payload(
                        this.id,
                        authorId,
                        authorType,
                        isInternalNote
                )
        ));
    }

    @Override
    public Long getId() {
        return id;
    }
}
