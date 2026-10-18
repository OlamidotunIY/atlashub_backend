package com.atlashub.support.application.command.AddTicketMessage;

import com.atlashub.support.domain.valueobject.AuthorType;
import java.util.List;

public record AddTicketMessageCommand(
        Long ticketId,
        Long authorId,
        AuthorType authorType,
        String message,
        boolean isInternalNote,
        List<String> attachmentUrls,
        String correlationId
) {}
