package com.atlashub.support.application.command.ReopenTicket;

public record ReopenTicketCommand(
        Long ticketId,
        Long userId,
        String reason,
        String correlationId
) {}
