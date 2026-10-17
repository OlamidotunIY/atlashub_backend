package com.atlashub.support.application.query.GetTicketDetails;

import com.atlashub.support.domain.valueobject.TicketCategory;
import com.atlashub.support.domain.valueobject.TicketPriority;
import com.atlashub.support.domain.valueobject.TicketStatus;
import java.time.ZonedDateTime;
import java.util.List;

public record TicketDetailsResult(
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
        ZonedDateTime updatedAt,
        List<TicketMessageResult> messages
) {}
