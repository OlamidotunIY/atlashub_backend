package com.atlashub.support.application.command.ResolveTicket;

public record ResolveTicketCommand(
        Long ticketId,
        Long agentId,
        String resolutionNote,
        String correlationId
) {}
