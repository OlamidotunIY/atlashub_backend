package com.atlashub.support.application.query.GetTicketDetails;

import com.atlashub.support.domain.valueobject.AuthorType;
import java.time.ZonedDateTime;
import java.util.List;

public record TicketMessageResult(
        Long id,
        Long ticketId,
        Long authorId,
        AuthorType authorType,
        String message,
        boolean isInternalNote,
        List<String> attachmentUrls,
        ZonedDateTime createdAt
) {}
