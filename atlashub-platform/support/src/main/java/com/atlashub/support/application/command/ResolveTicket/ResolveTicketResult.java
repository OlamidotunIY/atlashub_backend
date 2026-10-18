package com.atlashub.support.application.command.ResolveTicket;

import java.time.ZonedDateTime;

public record ResolveTicketResult(
        Long ticketId,
        String status,
        String resolutionNote,
        ZonedDateTime resolvedAt
) {}
