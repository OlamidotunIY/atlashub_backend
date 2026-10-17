package com.atlashub.support.application.query.ListMyTickets;

import com.atlashub.support.domain.valueobject.TicketCategory;
import com.atlashub.support.domain.valueobject.TicketPriority;
import com.atlashub.support.domain.valueobject.TicketStatus;
import java.time.ZonedDateTime;

public record TicketSummaryResult(
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
        ZonedDateTime firstResponseDeadline,
        ZonedDateTime resolutionDeadline,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {}
