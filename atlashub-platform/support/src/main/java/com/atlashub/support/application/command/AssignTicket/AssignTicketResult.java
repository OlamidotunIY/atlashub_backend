package com.atlashub.support.application.command.AssignTicket;

public record AssignTicketResult(
        Long ticketId,
        Long agentId,
        String status
) {}
