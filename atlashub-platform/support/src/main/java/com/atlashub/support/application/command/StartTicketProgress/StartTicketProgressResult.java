package com.atlashub.support.application.command.StartTicketProgress;

public record StartTicketProgressResult(
        Long ticketId,
        String status
) {}
