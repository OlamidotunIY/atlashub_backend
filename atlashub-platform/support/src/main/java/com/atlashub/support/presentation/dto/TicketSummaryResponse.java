package com.atlashub.support.presentation.dto;

import com.atlashub.support.domain.valueobject.TicketCategory;
import com.atlashub.support.domain.valueobject.TicketPriority;
import com.atlashub.support.domain.valueobject.TicketStatus;
import java.time.ZonedDateTime;

public record TicketSummaryResponse(
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
