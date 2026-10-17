package com.atlashub.support.application.command.CloseTicket;

public record CloseTicketCommand(
        Long ticketId,
        Long closedBy,
        String correlationId
) {}
