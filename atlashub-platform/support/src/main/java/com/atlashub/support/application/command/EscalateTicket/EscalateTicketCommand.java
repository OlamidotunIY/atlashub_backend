package com.atlashub.support.application.command.EscalateTicket;

public record EscalateTicketCommand(
        Long ticketId,
        Long newAgentId,
        String escalationReason,
        String correlationId
) {}
