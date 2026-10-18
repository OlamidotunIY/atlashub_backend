package com.atlashub.support.application.query.ListAgentTickets;

import com.atlashub.support.domain.valueobject.TicketStatus;

public record ListAgentTicketsQuery(
        Long agentId,
        TicketStatus status
) {}
