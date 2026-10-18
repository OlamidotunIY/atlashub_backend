package com.atlashub.support.application.command.OpenTicket;

import com.atlashub.support.domain.valueobject.TicketCategory;
import com.atlashub.support.domain.valueobject.TicketPriority;

public record OpenTicketCommand(
        Long organizationId,
        Long raisedBy,
        TicketCategory category,
        TicketPriority priority,
        String subject,
        String description,
        String relatedEntityType,
        String relatedEntityId,
        String correlationId
) {}
