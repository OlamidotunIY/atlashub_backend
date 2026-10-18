package com.atlashub.support.application.query.ListAllTickets;

import com.atlashub.support.domain.valueobject.TicketCategory;
import com.atlashub.support.domain.valueobject.TicketPriority;
import com.atlashub.support.domain.valueobject.TicketStatus;

public record ListAllTicketsQuery(
        TicketStatus status,
        TicketCategory category,
        TicketPriority priority,
        Boolean slaBreached
) {}
