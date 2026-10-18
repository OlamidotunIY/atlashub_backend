package com.atlashub.support.application.command.AssignTicket;

public record AssignTicketCommand(
        Long ticketId,
        Long agentId,
        String correlationId
) {}
