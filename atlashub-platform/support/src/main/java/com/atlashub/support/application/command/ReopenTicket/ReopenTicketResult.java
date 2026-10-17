package com.atlashub.support.application.command.ReopenTicket;

public record ReopenTicketResult(
        Long ticketId,
        String status
) {}
