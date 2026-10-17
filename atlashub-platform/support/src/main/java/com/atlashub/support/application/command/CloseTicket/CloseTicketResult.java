package com.atlashub.support.application.command.CloseTicket;

import java.time.ZonedDateTime;

public record CloseTicketResult(
        Long ticketId,
        String status,
        ZonedDateTime closedAt,
        Long closedBy
) {}
