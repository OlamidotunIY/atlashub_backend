package com.atlashub.support.application.query.ListMyTickets;

import com.atlashub.support.domain.valueobject.TicketStatus;

public record ListMyTicketsQuery(
        Long organizationId,
        TicketStatus status
) {}
