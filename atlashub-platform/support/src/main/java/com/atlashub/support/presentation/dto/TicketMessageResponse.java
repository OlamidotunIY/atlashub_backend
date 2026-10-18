package com.atlashub.support.presentation.dto;

import com.atlashub.support.domain.valueobject.AuthorType;
import java.time.ZonedDateTime;
import java.util.List;

public record TicketMessageResponse(
        Long id,
        Long ticketId,
        Long authorId,
        AuthorType authorType,
        String message,
        boolean isInternalNote,
        List<String> attachmentUrls,
        ZonedDateTime createdAt
) {}
