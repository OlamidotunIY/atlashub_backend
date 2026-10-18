package com.atlashub.support.application.command.StartTicketProgress;

public record StartTicketProgressCommand(
        Long ticketId,
        Long agentId
) {}
