package com.atlashub.support.application.command.EscalateTicket;

public record EscalateTicketResult(
        Long ticketId,
        Long assignedTo
) {}
