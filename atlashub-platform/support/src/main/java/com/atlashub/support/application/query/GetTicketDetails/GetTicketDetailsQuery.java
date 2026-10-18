package com.atlashub.support.application.query.GetTicketDetails;

public record GetTicketDetailsQuery(
        Long ticketId,
        Long requesterOrgId,
        boolean isAgent
) {}
